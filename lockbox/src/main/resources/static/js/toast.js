const Toast = {
    init: function() {
        if (!document.querySelector('.toast-container')) {
            const container = document.createElement('div');
            container.className = 'toast-container';
            document.body.appendChild(container);
        }
    },

    /**
     * Show a toast notification
     * @param {string} message - The message to display
     * @param {string} type - The type of toast (success, error, warning, info)
     * @param {Object} options - Additional options
     * @param {string} options.title - Optional title for the toast
     * @param {number} options.duration - Duration in ms before auto-hide (default: 3000)
     * @param {boolean} options.closable - Whether to show a close button (default: true)
     * @param {string} options.icon - HTML for a custom icon
     */
    show: function(message, type = 'info', options = {}) {
        this.init();
        
        // Default options
        const defaults = {
            title: '',
            duration: 3000,
            closable: true,
            icon: null
        };
        
        // Merge options with defaults
        const settings = {...defaults, ...options};
        
        // Create toast element
        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        
        // Create icon based on type
        let iconHtml = '';
        if (settings.icon) {
            iconHtml = settings.icon;
        } else {
            // Default icons based on type
            switch (type) {
                case 'success':
                    iconHtml = `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline></svg>`;
                    break;
                case 'error':
                    iconHtml = `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="15" y1="9" x2="9" y2="15"></line><line x1="9" y1="9" x2="15" y2="15"></line></svg>`;
                    break;
                case 'warning':
                    iconHtml = `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path><line x1="12" y1="9" x2="12" y2="13"></line><line x1="12" y1="17" x2="12.01" y2="17"></line></svg>`;
                    break;
                case 'info':
                default:
                    iconHtml = `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="16" x2="12" y2="12"></line><line x1="12" y1="8" x2="12.01" y2="8"></line></svg>`;
                    break;
            }
        }
        
        // Create toast content
        const content = `
            <div class="toast-icon">${iconHtml}</div>
            <div class="toast-content">
                ${settings.title ? `<div class="toast-title">${settings.title}</div>` : ''}
                <div class="toast-message">${message}</div>
            </div>
            ${settings.closable ? `<button class="toast-close">&times;</button>` : ''}
        `;
        toast.innerHTML = content;
        
        // Add toast to container
        const container = document.querySelector('.toast-container');
        container.appendChild(toast);
        
        // Add close button functionality
        if (settings.closable) {
            const closeBtn = toast.querySelector('.toast-close');
            closeBtn.addEventListener('click', () => {
                this.hide(toast);
            });
        }
        
        // Show the toast with a slight delay for smoother animation
        setTimeout(() => {
            toast.classList.add('show');
        }, 10);
        
        // Auto-hide toast after duration
        if (settings.duration > 0) {
            setTimeout(() => {
                this.hide(toast);
            }, settings.duration);
        }
        
        return toast;
    },
    
    /**
     * Hide a toast notification
     * @param {HTMLElement} toast - The toast element to hide
     */
    hide: function(toast) {
        toast.classList.remove('show');
        
        // Remove the element after animation completes
        toast.addEventListener('transitionend', () => {
            if (toast && toast.parentNode) {
                toast.parentNode.removeChild(toast);
            }
        }, { once: true });
    },
    
    /**
     * Show a success toast
     * @param {string} message - The message to display
     * @param {Object} options - Additional options
     */
    success: function(message, options = {}) {
        return this.show(message, 'success', {
            title: options.title || 'Success',
            ...options
        });
    },
    
    /**
     * Show an error toast
     * @param {string} message - The message to display
     * @param {Object} options - Additional options
     */
    error: function(message, options = {}) {
        return this.show(message, 'error', {
            title: options.title || 'Error',
            ...options
        });
    },
    
    /**
     * Show a warning toast
     * @param {string} message - The message to display
     * @param {Object} options - Additional options
     */
    warning: function(message, options = {}) {
        return this.show(message, 'warning', {
            title: options.title || 'Warning',
            ...options
        });
    },
    
    /**
     * Show an info toast
     * @param {string} message - The message to display
     * @param {Object} options - Additional options
     */
    info: function(message, options = {}) {
        return this.show(message, 'info', {
            title: options.title || 'Information',
            ...options
        });
    }
};

// Initialize the toast system when the DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    Toast.init();
}); 