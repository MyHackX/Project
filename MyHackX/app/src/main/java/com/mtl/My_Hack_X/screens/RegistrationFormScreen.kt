package com.mtl.My_Hack_X.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mtl.My_Hack_X.data.model.Event
import com.mtl.My_Hack_X.data.model.Hackathon
import com.mtl.My_Hack_X.data.model.RegistrationDetails
import com.mtl.My_Hack_X.data.model.TeamMember
import com.mtl.My_Hack_X.data.services.FirebaseService
import kotlinx.coroutines.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.draw.alpha

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationFormScreen(
    navController: NavController,
    eventId: String,
    isHackathon: Boolean
) {
    val firebaseService = remember { FirebaseService.getInstance() }
    val coroutineScope = rememberCoroutineScope()
    
    // State variables
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var event by remember { mutableStateOf<Event?>(null) }
    var hackathon by remember { mutableStateOf<Hackathon?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    
    // Form state variables
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var college by remember { mutableStateOf("") }
    var currentEducation by remember { mutableStateOf("") }
    var isGraduate by remember { mutableStateOf(false) }
    var fieldOfStudy by remember { mutableStateOf("") }
    var yearOfStudy by remember { mutableStateOf("") }
    
    // Team details (for hackathons)
    var isTeamRegistration by remember { mutableStateOf(false) }
    var teamName by remember { mutableStateOf("") }
    var teamMembers by remember { mutableStateOf(listOf<TeamMember>()) }
    
    // Additional fields
    var skills by remember { mutableStateOf("") }
    var previousExperience by remember { mutableStateOf("") }
    var portfolioLink by remember { mutableStateOf("") }
    var githubLink by remember { mutableStateOf("") }
    var linkedinProfile by remember { mutableStateOf("") }
    var tshirtSize by remember { mutableStateOf("M") }
    var additionalInfo by remember { mutableStateOf("") }
    
    // Dialog state
    var showTeamMemberDialog by remember { mutableStateOf(false) }
    var currentTeamMember by remember { mutableStateOf(TeamMember()) }
    var currentTeamMemberIndex by remember { mutableStateOf(-1) }
    
    // Load event/hackathon details
    LaunchedEffect(eventId, isHackathon) {
        isLoading = true
        errorMessage = null
        
        try {
            if (isHackathon) {
                val details = firebaseService.getHackathonById(eventId)
                if (details != null) {
                    hackathon = details
                } else {
                    errorMessage = "Hackathon not found"
                }
            } else {
                val details = firebaseService.getEventById(eventId)
                if (details != null) {
                    event = details
                } else {
                    errorMessage = "Event not found"
                }
            }
            
            // Pre-populate with user data if available
            val currentUser = firebaseService.getCurrentUserProfile()
            if (currentUser != null) {
                name = currentUser.name.ifEmpty { currentUser.displayName }
                email = currentUser.email
            }
        } catch (e: Exception) {
            errorMessage = "Error loading details: ${e.message}"
        } finally {
            isLoading = false
        }
    }
    
    // Function to validate form
    fun validateForm(): Boolean {
        if (name.isBlank()) {
            errorMessage = "Please enter your name"
            return false
        }
        if (email.isBlank()) {
            errorMessage = "Please enter your email"
            return false
        }
        if (phone.isBlank()) {
            errorMessage = "Please enter your phone number"
            return false
        }
        if (college.isBlank()) {
            errorMessage = "Please enter your college/university"
            return false
        }
        
        if (isHackathon && isTeamRegistration) {
            if (teamName.isBlank()) {
                errorMessage = "Please enter your team name"
                return false
            }
            if (teamMembers.isEmpty()) {
                errorMessage = "Please add at least one team member"
                return false
            }
        }
        
        return true
    }
    
    // Function to submit registration
    fun submitRegistration() {
        if (!validateForm()) return
        
        isLoading = true
        errorMessage = null
        
        coroutineScope.launch {
            try {
                val userId = firebaseService.getCurrentUserId()
                if (userId == null) {
                    errorMessage = "You must be logged in to register"
                    isLoading = false
                    return@launch
                }
                
                val skillsList = skills.split(",").map { it.trim() }.filter { it.isNotBlank() }
                
                val registrationDetails = RegistrationDetails(
                    userId = userId,
                    eventId = eventId,
                    isHackathon = isHackathon,
                    name = name,
                    email = email,
                    phone = phone,
                    college = college,
                    currentEducation = currentEducation,
                    isGraduate = isGraduate,
                    fieldOfStudy = fieldOfStudy,
                    yearOfStudy = yearOfStudy,
                    teamName = teamName,
                    teamMembers = teamMembers,
                    skills = skillsList,
                    previousExperience = previousExperience,
                    portfolioLink = portfolioLink,
                    githubLink = githubLink,
                    linkedinProfile = linkedinProfile,
                    tshirtSize = tshirtSize,
                    additionalInfo = additionalInfo
                )
                
                val success = if (isHackathon) {
                    firebaseService.registerUserForHackathon(userId, eventId, registrationDetails)
                } else {
                    firebaseService.registerUserForEvent(userId, eventId, registrationDetails)
                }
                
                if (success) {
                    successMessage = "Registration successful!"
                    // Navigate back after a delay
                    coroutineScope.launch {
                        kotlinx.coroutines.delay(1500)
                        navController.popBackStack()
                    }
                } else {
                    errorMessage = "Registration failed. Please try again."
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isHackathon) "Hackathon Registration" else "Event Registration") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (errorMessage != null) {
                // Error view
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = errorMessage ?: "An error occurred",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { navController.popBackStack() }) {
                        Text("Go Back")
                    }
                }
            } else if (successMessage != null) {
                // Success view
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = successMessage ?: "Registration successful!",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You will be redirected shortly...",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                // Registration form
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Event/Hackathon details
                    val title = if (isHackathon) hackathon?.title else event?.title
                    
                    Text(
                        text = "Registering for: $title",
                        style = MaterialTheme.typography.titleLarge
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Personal Information Section
                    Text(
                        text = "Personal Information",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name *") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email *") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number *") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Academic Information Section
                    Text(
                        text = "Academic Information",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    OutlinedTextField(
                        value = college,
                        onValueChange = { college = it },
                        label = { Text("College/University *") },
                        leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = currentEducation,
                        onValueChange = { currentEducation = it },
                        label = { Text("Current Education/Degree") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = isGraduate,
                            onCheckedChange = { isGraduate = it }
                        )
                        Text("I am a graduate")
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = fieldOfStudy,
                        onValueChange = { fieldOfStudy = it },
                        label = { Text("Field of Study/Expertise") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = yearOfStudy,
                        onValueChange = { yearOfStudy = it },
                        label = { Text("Year of Study") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Show team registration only for hackathons
                    if (isHackathon) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Team Information",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = isTeamRegistration,
                                onCheckedChange = { isTeamRegistration = it }
                            )
                            Text("I'm registering as part of a team")
                        }
                        
                        AnimatedVisibility(visible = isTeamRegistration) {
                            Column {
                                OutlinedTextField(
                                    value = teamName,
                                    onValueChange = { teamName = it },
                                    label = { Text("Team Name *") },
                                    leadingIcon = { Icon(Icons.Default.Group, contentDescription = null) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Team members list
                                Text(
                                    text = "Team Members",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                
                                teamMembers.forEachIndexed { index, member ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(text = member.name, style = MaterialTheme.typography.bodyLarge)
                                                Text(text = member.email, style = MaterialTheme.typography.bodySmall)
                                                Text(text = member.role, style = MaterialTheme.typography.bodySmall)
                                            }
                                            
                                            Row {
                                                IconButton(onClick = {
                                                    currentTeamMember = member
                                                    currentTeamMemberIndex = index
                                                    showTeamMemberDialog = true
                                                }) {
                                                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                                                }
                                                
                                                IconButton(onClick = {
                                                    teamMembers = teamMembers.toMutableList().apply { removeAt(index) }
                                                }) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Remove")
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Button(
                                    onClick = {
                                        currentTeamMember = TeamMember()
                                        currentTeamMemberIndex = -1
                                        showTeamMemberDialog = true
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Add Team Member")
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Additional Information
                    Text(
                        text = "Additional Information",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    OutlinedTextField(
                        value = skills,
                        onValueChange = { skills = it },
                        label = { Text("Skills (comma separated)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = previousExperience,
                        onValueChange = { previousExperience = it },
                        label = { Text("Previous Experience") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (isHackathon) {
                        OutlinedTextField(
                            value = githubLink,
                            onValueChange = { githubLink = it },
                            label = { Text("GitHub Profile URL") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    OutlinedTextField(
                        value = linkedinProfile,
                        onValueChange = { linkedinProfile = it },
                        label = { Text("LinkedIn Profile URL") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = portfolioLink,
                        onValueChange = { portfolioLink = it },
                        label = { Text("Portfolio/Website URL") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // T-shirt size (dropdown)
                    var expanded by remember { mutableStateOf(false) }
                    val sizes = listOf("XS", "S", "M", "L", "XL", "XXL")
                    
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = tshirtSize,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("T-Shirt Size") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            sizes.forEach { size ->
                                DropdownMenuItem(
                                    text = { Text(size) },
                                    onClick = {
                                        tshirtSize = size
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = additionalInfo,
                        onValueChange = { additionalInfo = it },
                        label = { Text("Additional Information") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    
                    Button(
                        onClick = { submitRegistration() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Submit Registration")
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
    
    // Team member dialog
    if (showTeamMemberDialog) {
        var teamMemberName by remember { mutableStateOf(currentTeamMember.name) }
        var teamMemberEmail by remember { mutableStateOf(currentTeamMember.email) }
        var teamMemberPhone by remember { mutableStateOf(currentTeamMember.phone) }
        var teamMemberCollege by remember { mutableStateOf(currentTeamMember.college) }
        var teamMemberRole by remember { mutableStateOf(currentTeamMember.role) }
        var dialogError by remember { mutableStateOf<String?>(null) }
        
        AlertDialog(
            onDismissRequest = { showTeamMemberDialog = false },
            title = { Text(if (currentTeamMemberIndex == -1) "Add Team Member" else "Edit Team Member") },
            text = {
                Column {
                    OutlinedTextField(
                        value = teamMemberName,
                        onValueChange = { teamMemberName = it },
                        label = { Text("Name *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = teamMemberEmail,
                        onValueChange = { teamMemberEmail = it },
                        label = { Text("Email *") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = teamMemberPhone,
                        onValueChange = { teamMemberPhone = it },
                        label = { Text("Phone") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = teamMemberCollege,
                        onValueChange = { teamMemberCollege = it },
                        label = { Text("College/University") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = teamMemberRole,
                        onValueChange = { teamMemberRole = it },
                        label = { Text("Role in Team") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    if (dialogError != null) {
                        Text(
                            text = dialogError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Validate
                        if (teamMemberName.isBlank()) {
                            dialogError = "Please enter team member name"
                            return@Button
                        }
                        if (teamMemberEmail.isBlank()) {
                            dialogError = "Please enter team member email"
                            return@Button
                        }
                        
                        val newMember = TeamMember(
                            name = teamMemberName,
                            email = teamMemberEmail,
                            phone = teamMemberPhone,
                            college = teamMemberCollege,
                            role = teamMemberRole
                        )
                        
                        if (currentTeamMemberIndex == -1) {
                            // Add new
                            teamMembers = teamMembers + newMember
                        } else {
                            // Update existing
                            teamMembers = teamMembers.toMutableList().apply {
                                set(currentTeamMemberIndex, newMember)
                            }
                        }
                        
                        showTeamMemberDialog = false
                    }
                ) {
                    Text(if (currentTeamMemberIndex == -1) "Add" else "Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTeamMemberDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AnimatedVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    androidx.compose.animation.AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        content()
    }
} 