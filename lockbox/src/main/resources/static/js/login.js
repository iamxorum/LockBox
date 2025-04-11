// LockBox Neo Modern UI - JavaScript
document.addEventListener('DOMContentLoaded', () => {
    // Initialize all features
    manageAnimations();
    createBackgroundEffect();
    initFormInteractions();
    initPasswordToggle();
    initButtonEffect();
    initCardAnimation();
    initThemeToggle();
  });
  
  // Create subtle background gradient blobs
  function createBackgroundEffect() {
    const container = document.querySelector('.bg-gradients');
    if (!container) return;
    
    // Create gradient blobs
    const colors = [
      'rgba(139, 92, 246, 0.5)', // Purple (primary)
      'rgba(167, 139, 250, 0.4)', // Light purple
      'rgba(124, 58, 237, 0.5)'  // Dark purple
    ];
    
    for (let i = 0; i < 3; i++) {
      const blob = document.createElement('div');
      blob.classList.add('gradient-blob');
      
      // Random size between 300px and 500px
      const size = Math.floor(Math.random() * 200) + 300;
      blob.style.width = `${size}px`;
      blob.style.height = `${size}px`;
      
      // Random position
      blob.style.top = `${Math.random() * 100}%`;
      blob.style.left = `${Math.random() * 100}%`;
      blob.style.transform = `translate(-50%, -50%)`;
      
      // Set color
      blob.style.backgroundColor = colors[i % colors.length];
      
      // Add subtle float animation
      const duration = 15 + (Math.random() * 20);
      const delay = Math.random() * 5;
      blob.style.animation = `float ${duration}s ease-in-out ${delay}s infinite alternate`;
      
      container.appendChild(blob);
    }
  }
  
  // Form interaction enhancements
  function initFormInteractions() {
    const inputs = document.querySelectorAll('.form-control');
    
    inputs.forEach(input => {
      // Handle focus state
      input.addEventListener('focus', () => {
        input.parentElement.classList.add('focused');
      });
      
      input.addEventListener('blur', () => {
        input.parentElement.classList.remove('focused');
      });
      
      // Check initial state (if field has value on page load)
      if (input.value) {
        input.classList.add('has-value');
      }
      
      // Update state on input
      input.addEventListener('input', () => {
        if (input.value) {
          input.classList.add('has-value');
        } else {
          input.classList.remove('has-value');
        }
      });
    });
    
    // Additional check on page load
    setTimeout(() => {
      inputs.forEach(input => {
        if (input.value) {
          input.classList.add('has-value');
        }
      });
    }, 100);
  }
  
  // Password toggle functionality
  function initPasswordToggle() {
    const passwordToggle = document.querySelector('.password-toggle');
    if (!passwordToggle) return;
    
    passwordToggle.addEventListener('click', (e) => {
      e.preventDefault();
      e.stopPropagation();
      
      const passwordInput = document.getElementById('password');
      const currentType = passwordInput.getAttribute('type');
      
      if (currentType === 'password') {
        passwordInput.setAttribute('type', 'text');
        passwordToggle.innerHTML = `
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
            <line x1="1" y1="1" x2="23" y2="23"></line>
          </svg>
        `;
      } else {
        passwordInput.setAttribute('type', 'password');
        passwordToggle.innerHTML = `
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
            <circle cx="12" cy="12" r="3"></circle>
          </svg>
        `;
      }
      
      // Refocus on input without causing layout shifts
      setTimeout(() => {
        passwordInput.focus();
      }, 10);
      
      return false;
    });
  }
  
  // Button interaction effects
  function initButtonEffect() {
    const buttons = document.querySelectorAll('.btn');
    
    buttons.forEach(button => {
      // Add subtle hover effect
      button.addEventListener('mouseenter', () => {
        button.style.transform = 'translateY(-1px)';
      });
      
      button.addEventListener('mouseleave', () => {
        button.style.transform = '';
      });
      
      // Add loading state to form submit
      if (button.type === 'submit') {
        const form = button.closest('form');
        
        if (form) {
          form.addEventListener('submit', () => {
            // Save original text
            const originalText = button.innerHTML;
            
            // Replace with loading spinner
            button.disabled = true;
            button.innerHTML = `
              <svg class="spinner" width="20" height="20" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                <circle class="spinner-path" cx="12" cy="12" r="10" fill="none" stroke="currentColor" stroke-width="3"></circle>
              </svg>
            `;
            
            // Add inline animation
            const style = document.createElement('style');
            style.textContent = `
              .spinner { animation: rotate 1.5s linear infinite; }
              .spinner-path { 
                stroke-dasharray: 60, 150;
                stroke-dashoffset: 0;
                animation: dash 1.5s ease-in-out infinite;
                stroke-linecap: round;
              }
              @keyframes rotate {
                100% { transform: rotate(360deg); }
              }
              @keyframes dash {
                0% {
                  stroke-dasharray: 1, 150;
                  stroke-dashoffset: 0;
                }
                50% {
                  stroke-dasharray: 90, 150;
                  stroke-dashoffset: -35;
                }
                100% {
                  stroke-dasharray: 90, 150;
                  stroke-dashoffset: -124;
                }
              }
            `;
            document.head.appendChild(style);
          });
        }
      }
    });
  }
  
  // Card animation on load
  function initCardAnimation() {
    const card = document.querySelector('.card');
    if (!card) return;
    
    // Add initial animation on page load
    card.classList.add('scale-in');
    
    // Add subtle hover effect
    card.addEventListener('mouseenter', () => {
      card.style.transform = 'translateY(-5px)';
      card.style.boxShadow = 'var(--shadow-lg)';
    });
    
    card.addEventListener('mouseleave', () => {
      card.style.transform = '';
      card.style.boxShadow = 'var(--shadow-md)';
    });
  }
  
  // Helper: Adds a float animation for decorative elements
  function addFloatAnimation() {
    const elementsToFloat = document.querySelectorAll('.float');
    
    elementsToFloat.forEach(element => {
      // Random animation parameters for natural movement
      const duration = 3 + Math.random() * 2;
      const delay = Math.random() * 2;
      const distance = 5 + Math.random() * 5;
      
      element.style.animation = `float ${duration}s ease-in-out ${delay}s infinite alternate`;
      
      // Add keyframes if they don't exist
      if (!document.querySelector('style#float-keyframes')) {
        const style = document.createElement('style');
        style.id = 'float-keyframes';
        style.textContent = `
          @keyframes float {
            0% { transform: translateY(0); }
            100% { transform: translateY(-${distance}px); }
          }
        `;
        document.head.appendChild(style);
      }
    });
  }
  
  // Initialize the theme toggle
  function initThemeToggle() {
    const themeSwitch = document.getElementById('theme-switch');
    if (!themeSwitch) return;
    
    // Check for saved theme preference or use device preference
    const savedTheme = localStorage.getItem('theme');
    
    // If we have a saved theme preference, apply it
    if (savedTheme) {
      document.documentElement.setAttribute('data-theme', savedTheme);
    } else {
      // If no saved preference, check if user's device has dark mode enabled
      const prefersDarkMode = window.matchMedia('(prefers-color-scheme: dark)').matches;
      document.documentElement.setAttribute('data-theme', prefersDarkMode ? 'dark' : 'light');
    }
    
    // Toggle theme when the button is clicked
    themeSwitch.addEventListener('click', () => {
      const currentTheme = document.documentElement.getAttribute('data-theme') || 
                           (window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light');
      
      // Switch to the opposite theme
      const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
      
      // Apply the theme change
      document.documentElement.setAttribute('data-theme', newTheme);
      
      // Save the preference
      localStorage.setItem('theme', newTheme);
      
      // Animate the theme switch
      themeSwitch.classList.add('rotate');
      setTimeout(() => {
        themeSwitch.classList.remove('rotate');
      }, 500);
    });
  }
  
  // Manage animations to prevent flash on page load
  function manageAnimations() {
    // Remove preload class to allow transitions
    document.body.classList.remove('preload');
    
    // First, ensure everything is visible immediately
    document.body.style.opacity = "1";
    
    // Add a class to the html element to indicate page has loaded
    document.documentElement.classList.add('page-loaded');
    
    // Get all animated elements
    const animatedElements = document.querySelectorAll('.fade-in, .fade-up, .scale-in, .fade-graceful, .appear-bottom, .appear-left, .appear-right, .typing');
    
    // First force everything to be visible in final state
    animatedElements.forEach(el => {
      el.style.opacity = "1";
      el.style.transform = "none";
    });
    
    // On DOMContentLoaded, content is shown static first,
    // then after a short delay, we animate from initial state
    setTimeout(() => {
      // Clear forced styles to enable animations
      animatedElements.forEach(el => {
        el.style.opacity = "";
        el.style.transform = "";
      });
      
      // Now apply animation classes
      document.documentElement.classList.add('animations-enabled');
    }, 300); // Longer delay to ensure page is fully rendered
  } 