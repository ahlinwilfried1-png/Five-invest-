package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ActivityLogEntity
import com.example.data.AppRepository
import com.example.data.DatabaseProvider
import com.example.data.DepositEntity
import com.example.data.InvestmentEntity
import com.example.data.NotificationEntity
import com.example.data.ProductEntity
import com.example.data.UserEntity
import com.example.data.WithdrawalEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository = DatabaseProvider.getRepository(application)

    // Current session
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    // Loading states
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // UI event notifications
    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent = _toastEvent.asSharedFlow()

    // Auth screen states
    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _isOtpRequired = MutableStateFlow(false)
    val isOtpRequired: StateFlow<Boolean> = _isOtpRequired.asStateFlow()

    // Global Flows
    val products: StateFlow<List<ProductEntity>> = repository.getAllProductsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Admin Specific Flows
    val allUsers: StateFlow<List<UserEntity>> = repository.getAllUsersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDeposits: StateFlow<List<DepositEntity>> = repository.getAllDepositsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWithdrawals: StateFlow<List<WithdrawalEntity>> = repository.getAllWithdrawalsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allInvestments: StateFlow<List<InvestmentEntity>> = repository.getAllInvestmentsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLogs: StateFlow<List<ActivityLogEntity>> = repository.getAllLogsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // User Session Specific Flows (Reacting to login/logout)
    val userInvestments: StateFlow<List<InvestmentEntity>> = _currentUser
        .flatMapLatest { user ->
            if (user != null) repository.getInvestmentsForUserFlow(user.id)
            else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userDeposits: StateFlow<List<DepositEntity>> = _currentUser
        .flatMapLatest { user ->
            if (user != null) repository.getDepositsForUserFlow(user.id)
            else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userWithdrawals: StateFlow<List<WithdrawalEntity>> = _currentUser
        .flatMapLatest { user ->
            if (user != null) repository.getWithdrawalsForUserFlow(user.id)
            else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userLogs: StateFlow<List<ActivityLogEntity>> = _currentUser
        .flatMapLatest { user ->
            if (user != null) repository.getLogsForUserFlow(user.id)
            else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userNotifications: StateFlow<List<NotificationEntity>> = _currentUser
        .flatMapLatest { user ->
            if (user != null) repository.getNotificationsForUserFlow(user.id)
            else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val teamUsers: StateFlow<List<UserEntity>> = _currentUser
        .flatMapLatest { user ->
            if (user != null) repository.getTeamForUserFlow(user.referralCode)
            else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Run database initializer
        viewModelScope.launch {
            repository.seedInitialDataIfNecessary()
        }
    }

    private fun showToast(msg: String) {
        viewModelScope.launch {
            _toastEvent.emit(msg)
        }
    }

    fun clearAuthError() {
        _authError.value = null
    }

    // --- Authentication Actions ---
    fun testSwitchRole(toAdmin: Boolean) {
        viewModelScope.launch {
            val email = if (toAdmin) "admin@gmail.com" else "user@gmail.com"
            val res = repository.loginUser(email, if (toAdmin) "admin" else "user")
            res.onSuccess { user ->
                _currentUser.value = user
                _isOtpRequired.value = false
                showToast("Simulé en tant que ${user.name} (${if (user.isAdmin) "Admin" else "Utilisateur"})")
            }.onFailure {
                showToast("Échec du basculement rapide.")
            }
        }
    }

    fun login(emailOrPhone: String, passwordRaw: String, onNavigateHome: () -> Unit) {
        if (emailOrPhone.isBlank() || passwordRaw.isBlank()) {
            _authError.value = "Veuillez remplir tous les champs."
            return
        }

        _isLoading.value = true
        _authError.value = null

        viewModelScope.launch {
            val result = repository.loginUser(emailOrPhone.trim(), passwordRaw)
            _isLoading.value = false
            result.onSuccess { user ->
                _currentUser.value = user
                if (!user.isOtpVerified) {
                    _isOtpRequired.value = true
                    repository.requestOtp(user.id)
                    showToast("Code OTP envoyé pour vérification.")
                } else {
                    showToast("Heureux de vous revoir, ${user.name} !")
                    onNavigateHome()
                }
            }.onFailure { exception ->
                _authError.value = exception.message ?: "Une erreur de connexion est survenue."
            }
        }
    }

    fun register(
        name: String,
        email: String,
        phone: String,
        passwordRaw: String,
        referralCode: String?,
        onNavigateOtp: () -> Unit
    ) {
        if (name.isBlank() || email.isBlank() || phone.isBlank() || passwordRaw.isBlank()) {
            _authError.value = "Veuillez remplir tous les champs obligatoires."
            return
        }

        _isLoading.value = true
        _authError.value = null

        viewModelScope.launch {
            val result = repository.registerUser(
                name = name.trim(),
                email = email.trim(),
                phone = phone.trim(),
                passwordRaw = passwordRaw,
                referredByCode = if (referralCode.isNullOrBlank()) null else referralCode.trim()
            )
            _isLoading.value = false
            result.onSuccess { user ->
                _currentUser.value = user
                _isOtpRequired.value = true
                repository.requestOtp(user.id)
                showToast("Veuillez saisir le code OTP envoyé.")
                onNavigateOtp()
            }.onFailure { exception ->
                _authError.value = exception.message ?: "Une erreur s'est produite lors de l'inscription."
            }
        }
    }

    fun verifyOtp(code: String, onNavigateHome: () -> Unit) {
        val user = _currentUser.value ?: return
        if (code.isBlank() || code.length < 4) {
            _authError.value = "Veuillez entrer un code de sécurité valide."
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            val verified = repository.verifyOtp(user.id, code.trim())
            _isLoading.value = false
            if (verified) {
                // Refresh session user state
                val fresh = repository.getUserById(user.id)
                _currentUser.value = fresh
                _isOtpRequired.value = false
                showToast("Compte vérifié avec succès !")
                onNavigateHome()
            } else {
                _authError.value = "Code de vérification OTP erroné."
            }
        }
    }

    fun resendOtp() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.requestOtp(user.id)
            showToast("Un nouveau code OTP a été envoyé.")
        }
    }

    fun forgotPassword(emailOrPhone: String, otpCode: String, newPassRaw: String, onNavigateLogin: () -> Unit) {
        if (emailOrPhone.isBlank() || otpCode.isBlank() || newPassRaw.isBlank()) {
            _authError.value = "Tous les champs sont requis pour réinitialiser le mot de passe."
            return
        }

        _isLoading.value = true
        _authError.value = null

        viewModelScope.launch {
            val result = repository.resetPassword(emailOrPhone.trim(), otpCode.trim(), newPassRaw)
            _isLoading.value = false
            result.onSuccess {
                showToast("Mot de passe réinitialisé. Connectez-vous avec vos nouveaux identifiants.")
                onNavigateLogin()
            }.onFailure { exception ->
                _authError.value = exception.message ?: "Échec de réinitialisation."
            }
        }
    }

    fun logout(onNavigateAuth: () -> Unit) {
        _currentUser.value = null
        _isOtpRequired.value = false
        showToast("Déconnexion réussie.")
        onNavigateAuth()
    }

    // --- User Portfolio Interactions ---
    fun investInProduct(product: ProductEntity) {
        val user = _currentUser.value ?: return
        _isLoading.value = true
        viewModelScope.launch {
            val res = repository.purchaseProduct(user.id, product.id)
            _isLoading.value = false
            res.onSuccess { investment ->
                // refresh current user model
                val updated = repository.getUserById(user.id)
                _currentUser.value = updated
                showToast("Félicitations ! Votre contrat '${product.name}' est maintenant actif.")
            }.onFailure { e ->
                showToast("Échec de l'investissement: ${e.message}")
            }
        }
    }

    fun claimDailyYields() {
        val user = _currentUser.value ?: return
        _isLoading.value = true
        viewModelScope.launch {
            val claimCount = repository.claimDailyYields(user.id)
            _isLoading.value = false
            if (claimCount > 0) {
                val updated = repository.getUserById(user.id)
                _currentUser.value = updated
                showToast("Profits collectés: Rendements journaliers de $claimCount contrat(s) encaissés !")
            } else {
                showToast("Aucun rendement disponible actuellement pour un encaissement.")
            }
        }
    }

    fun submitDeposit(amount: Double) {
        val user = _currentUser.value ?: return
        if (amount < 2000) {
            showToast("Le montant minimum de dépôt est de 2 000 F CFA.")
            return
        }
        _isLoading.value = true
        viewModelScope.launch {
            // mock a gallery/camera proof submission file path
            val mockProofName = "PROUVE_DEP_${System.currentTimeMillis()}.png"
            repository.submitDeposit(user.id, amount, mockProofName)
            _isLoading.value = false
            showToast("Dépôt soumis ! L'admin créditera votre compte dès réception du Mobile Money.")
        }
    }

    fun submitWithdrawal(amount: Double, provider: String, number: String) {
        val user = _currentUser.value ?: return
        if (amount < 1000) {
            showToast("Le montant minimum de retrait est de 1 000 F CFA.")
            return
        }
        if (number.isBlank() || provider.isBlank()) {
            showToast("Veuillez renseigner votre opérateur et numéro Mobile Money.")
            return
        }
        _isLoading.value = true
        viewModelScope.launch {
            val res = repository.submitWithdrawal(user.id, amount, provider, number)
            _isLoading.value = false
            res.onSuccess {
                val updated = repository.getUserById(user.id)
                _currentUser.value = updated
                showToast("Demande de retrait de $amount F CFA soumise avec succès.")
            }.onFailure { e ->
                showToast("Échec du retrait: ${e.message}")
            }
        }
    }

    fun updateProfile(name: String, provider: String, number: String) {
        val user = _currentUser.value ?: return
        if (name.isBlank()) {
            showToast("Veuillez renseigner un nom valide.")
            return
        }
        _isLoading.value = true
        viewModelScope.launch {
            val updated = user.copy(
                name = name,
                mobileMoneyProvider = provider,
                mobileMoneyNumber = number
            )
            repository.updateUser(updated)
            _currentUser.value = updated
            _isLoading.value = false
            showToast("Profil et informations de retrait mis à jour.")
        }
    }

    fun changePassword(oldPass: String, newPass: String) {
        val user = _currentUser.value ?: return
        if (newPass.isBlank() || oldPass.isBlank()) {
            showToast("Veuillez remplir les deux champs de mot de passe.")
            return
        }
        _isLoading.value = true
        viewModelScope.launch {
            val hashedOld = repository.hashPassword(oldPass)
            if (user.passwordHash != hashedOld) {
                showToast("Ancien mot de passe incorrect.")
                _isLoading.value = false
                return@launch
            }
            val updated = user.copy(
                passwordHash = repository.hashPassword(newPass)
            )
            repository.updateUser(updated)
            _currentUser.value = updated
            _isLoading.value = false
            showToast("Votre mot de passe a bien été modifié.")
        }
    }

    // --- Administration Actions ---
    fun blockUnblockUser(targetUser: UserEntity) {
        viewModelScope.launch {
            val updated = targetUser.copy(isBlocked = !targetUser.isBlocked)
            repository.updateUser(updated)
            showToast("Utilisateur ${targetUser.name}: ${if (updated.isBlocked) "Bloqué" else "Débloqué"}")
        }
    }

    fun modifyUserBalance(targetUser: UserEntity, amountOffset: Double) {
        viewModelScope.launch {
            val updated = targetUser.copy(balance = maxOf(0.0, targetUser.balance + amountOffset))
            repository.updateUser(updated)
            showToast("Solde ajusté pour ${targetUser.name}: Nouveau solde = ${updated.balance} FCFA")
        }
    }

    fun deleteUser(targetUser: UserEntity) {
        viewModelScope.launch {
            repository.deleteUser(targetUser)
            showToast("Compte utilisateur de ${targetUser.name} supprimé définitivement.")
        }
    }

    fun approveDeposit(depositId: Int, notes: String) {
        viewModelScope.launch {
            val res = repository.validateDepositByAdmin(depositId, notes, true)
            if (res) {
                showToast("Dépôt approuvé et crédité !")
                // If logged in user is admin, we don't need to refresh currentUser balance,
                // but if we are testing from a specific profile, we can trigger active user refresh.
                _currentUser.value?.let { currentUser ->
                    val fresh = repository.getUserById(currentUser.id)
                    _currentUser.value = fresh
                }
            } else {
                showToast("Échec de traitement du dépôt.")
            }
        }
    }

    fun rejectDeposit(depositId: Int, notes: String) {
        viewModelScope.launch {
            val res = repository.validateDepositByAdmin(depositId, notes, false)
            if (res) {
                showToast("Dépôt refusé.")
            } else {
                showToast("Échec de traitement du dépôt.")
            }
        }
    }

    fun approveWithdrawal(withdrawalId: Int, notes: String) {
        viewModelScope.launch {
            val res = repository.validateWithdrawalByAdmin(withdrawalId, notes, true)
            if (res) {
                showToast("Retrait validé comme payé !")
            } else {
                showToast("Échec de traitement du retrait.")
            }
        }
    }

    fun rejectWithdrawal(withdrawalId: Int, notes: String) {
        viewModelScope.launch {
            val res = repository.validateWithdrawalByAdmin(withdrawalId, notes, false)
            if (res) {
                showToast("Retrait refusé et fonds recrédités chez l'utilisateur.")
                _currentUser.value?.let { currentUser ->
                    val fresh = repository.getUserById(currentUser.id)
                    _currentUser.value = fresh
                }
            } else {
                showToast("Échec de traitement du retrait.")
            }
        }
    }

    fun createProduct(
        name: String,
        price: Double,
        dailyIncome: Double,
        duration: Int,
        bonus: Double,
        limit: Int,
        imageType: String
    ) {
        if (name.isBlank() || price <= 0 || dailyIncome <= 0 || duration <= 0) {
            showToast("Veuillez saisir des informations de produit valides.")
            return
        }
        viewModelScope.launch {
            val prod = ProductEntity(
                name = name,
                price = price,
                dailyIncome = dailyIncome,
                durationDays = duration,
                bonus = bonus,
                purchaseLimit = limit,
                isActive = true,
                imageType = imageType
            )
            repository.insertProduct(prod)
            showToast("Produit '$name' créé avec succès !")
        }
    }

    fun updateProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.updateProduct(product)
            showToast("Produit '${product.name}' mis à jour !")
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            showToast("Produit '${product.name}' supprimé.")
        }
    }

    fun toggleProductStatus(product: ProductEntity) {
        viewModelScope.launch {
            val updated = product.copy(isActive = !product.isActive)
            repository.updateProduct(updated)
            showToast("Produit '${product.name}' : ${if (updated.isActive) "Activé" else "Désactivé"}")
        }
    }

    fun postGlobalBroadcast(title: String, message: String) {
        if (title.isBlank() || message.isBlank()) {
            showToast("Veuillez remplir le titre et le corps du message.")
            return
        }
        viewModelScope.launch {
            repository.triggerBroadcastNotification(title, message)
            showToast("Message diffusé à l'ensemble des membres !")
        }
    }
}
