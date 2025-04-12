// LockBox - Modern 2025 Background Effects
document.addEventListener('DOMContentLoaded', () => {
  // Initialize modern background effects
  createModernGradients();
  createParticleField();
  createNeuralNetworkEffect();
  createLightFlares();
  
  // Initialize UI candy elements
  initClock();
  initDynamicGreeting();
});

// Create dynamic gradient background with subtle animation
function createModernGradients() {
  const container = document.querySelector('.bg-gradients');
  if (!container) return;
  
  // Create modern gradient blobs with better color scheme
  const colors = [
    'rgba(139, 92, 246, 0.3)', // Purple (primary) - more transparent
    'rgba(79, 70, 229, 0.25)', // Indigo
    'rgba(236, 72, 153, 0.2)'  // Pink - softer
  ];
  
  for (let i = 0; i < 3; i++) {
    const blob = document.createElement('div');
    blob.classList.add('gradient-blob');
    
    // Larger, more subtle blobs
    const size = Math.floor(Math.random() * 300) + 400;
    blob.style.width = `${size}px`;
    blob.style.height = `${size}px`;
    
    // Strategic position for better coverage
    blob.style.top = `${20 + (Math.random() * 60)}%`;
    blob.style.left = `${20 + (Math.random() * 60)}%`;
    blob.style.transform = `translate(-50%, -50%)`;
    
    // Set color with more blur for diffusion
    blob.style.backgroundColor = colors[i % colors.length];
    blob.style.filter = `blur(${80 + Math.random() * 40}px)`;
    
    // More subtle animation
    const duration = 20 + (Math.random() * 30);
    const delay = Math.random() * 5;
    blob.style.animation = `floatSlow ${duration}s ease-in-out ${delay}s infinite alternate`;
    
    container.appendChild(blob);
  }
  
  // Add keyframes for slower, more elegant floating
  if (!document.querySelector('#gradient-keyframes')) {
    const style = document.createElement('style');
    style.id = 'gradient-keyframes';
    style.textContent = `
      @keyframes floatSlow {
        0% { transform: translate(-50%, -50%) scale(1); }
        50% { transform: translate(-55%, -45%) scale(1.05); }
        100% { transform: translate(-45%, -55%) scale(0.95); }
      }
    `;
    document.head.appendChild(style);
  }
}

// Create subtle floating particles (data visualization style)
function createParticleField() {
  const container = document.querySelector('.bg-gradients');
  if (!container) return;
  
  // Create particle container
  const particleContainer = document.createElement('div');
  particleContainer.classList.add('particle-container');
  particleContainer.style.position = 'absolute';
  particleContainer.style.top = '0';
  particleContainer.style.left = '0';
  particleContainer.style.width = '100%';
  particleContainer.style.height = '100%';
  particleContainer.style.overflow = 'hidden';
  particleContainer.style.pointerEvents = 'none';
  
  // Create particles
  const particleCount = window.innerWidth < 768 ? 30 : 50;
  
  for (let i = 0; i < particleCount; i++) {
    const particle = document.createElement('div');
    particle.classList.add('particle');
    
    // Tiny sizes for a subtle effect
    const size = Math.random() * 4 + 1;
    particle.style.width = `${size}px`;
    particle.style.height = `${size}px`;
    
    // Random positions across the entire viewport
    particle.style.top = `${Math.random() * 100}%`;
    particle.style.left = `${Math.random() * 100}%`;
    
    // Modern styling
    particle.style.position = 'absolute';
    particle.style.borderRadius = '50%';
    
    // Random opacity and color
    const opacity = Math.random() * 0.5 + 0.1;
    
    // Dynamic colors based on position to create a depth effect
    const hue = (parseInt(particle.style.left) + parseInt(particle.style.top)) % 60 + 230;
    particle.style.backgroundColor = `hsla(${hue}, 70%, 70%, ${opacity})`;
    
    // Animation with random duration and delay
    const duration = Math.random() * 15 + 10;
    const delay = Math.random() * 5;
    particle.style.animation = `floatParticle ${duration}s ease-in-out ${delay}s infinite`;
    
    particleContainer.appendChild(particle);
  }
  
  container.appendChild(particleContainer);
  
  // Add keyframes for particle animation
  if (!document.querySelector('#particle-keyframes')) {
    const style = document.createElement('style');
    style.id = 'particle-keyframes';
    style.textContent = `
      @keyframes floatParticle {
        0% { transform: translate(0, 0); }
        25% { transform: translate(${Math.random() * 30 - 15}px, ${Math.random() * 30 - 15}px); }
        50% { transform: translate(${Math.random() * 30 - 15}px, ${Math.random() * 30 - 15}px); }
        75% { transform: translate(${Math.random() * 30 - 15}px, ${Math.random() * 30 - 15}px); }
        100% { transform: translate(0, 0); }
      }
    `;
    document.head.appendChild(style);
  }
}

// Create neural network-like connecting lines effect
function createNeuralNetworkEffect() {
  const container = document.querySelector('.bg-gradients');
  if (!container) return;
  
  // Create canvas for network effect
  const canvas = document.createElement('canvas');
  canvas.classList.add('neural-network');
  canvas.style.position = 'absolute';
  canvas.style.top = '0';
  canvas.style.left = '0';
  canvas.style.width = '100%';
  canvas.style.height = '100%';
  canvas.style.opacity = '0.15';
  canvas.style.pointerEvents = 'none';
  container.appendChild(canvas);
  
  // Set canvas size
  canvas.width = window.innerWidth;
  canvas.height = window.innerHeight;
  const ctx = canvas.getContext('2d');
  
  // Create nodes for network
  const nodeCount = window.innerWidth < 768 ? 15 : 25;
  const nodes = [];
  
  for (let i = 0; i < nodeCount; i++) {
    nodes.push({
      x: Math.random() * canvas.width,
      y: Math.random() * canvas.height,
      vx: Math.random() * 0.2 - 0.1,
      vy: Math.random() * 0.2 - 0.1,
      radius: Math.random() * 2 + 1
    });
  }
  
  // Connection distance threshold
  const connectionDistance = 150;
  
  // Animation loop
  function animate() {
    // Clear canvas
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    
    // Update and draw nodes
    for (let i = 0; i < nodes.length; i++) {
      const node = nodes[i];
      
      // Move node
      node.x += node.vx;
      node.y += node.vy;
      
      // Bounce off edges
      if (node.x < 0 || node.x > canvas.width) node.vx *= -1;
      if (node.y < 0 || node.y > canvas.height) node.vy *= -1;
      
      // Draw node
      ctx.beginPath();
      ctx.arc(node.x, node.y, node.radius, 0, Math.PI * 2);
      ctx.fillStyle = 'rgba(139, 92, 246, 0.5)';
      ctx.fill();
      
      // Draw connections
      for (let j = i + 1; j < nodes.length; j++) {
        const otherNode = nodes[j];
        const dx = otherNode.x - node.x;
        const dy = otherNode.y - node.y;
        const distance = Math.sqrt(dx * dx + dy * dy);
        
        if (distance < connectionDistance) {
          // Calculate opacity based on distance
          const opacity = 1 - (distance / connectionDistance);
          
          // Draw line
          ctx.beginPath();
          ctx.moveTo(node.x, node.y);
          ctx.lineTo(otherNode.x, otherNode.y);
          ctx.strokeStyle = `rgba(139, 92, 246, ${opacity * 0.15})`;
          ctx.lineWidth = 0.5;
          ctx.stroke();
        }
      }
    }
    
    // Continue animation
    requestAnimationFrame(animate);
  }
  
  // Start animation
  animate();
  
  // Handle window resize
  window.addEventListener('resize', () => {
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
  });
}

// Create enhanced light flares with 2025 style
function createLightFlares() {
  const container = document.querySelector('.bg-gradients');
  if (!container) return;
  
  // Create light flares with better styling
  for (let i = 0; i < 7; i++) {
    const flare = document.createElement('div');
    flare.classList.add('light-flare');
    
    // Varied sizes for flares
    const size = Math.floor(Math.random() * 200) + 100;
    flare.style.width = `${size}px`;
    flare.style.height = `${size}px`;
    
    // Strategic position
    flare.style.top = `${Math.random() * 100}%`;
    flare.style.left = `${Math.random() * 100}%`;
    
    // Enhanced styling
    const opacity = (Math.random() * 0.3) + 0.05;
    
    // Create complex gradient for more modern look
    const hue1 = Math.floor(Math.random() * 60) + 230; // Blue-purple range
    const hue2 = hue1 + 30;
    
    flare.style.background = `radial-gradient(
      circle, 
      hsla(${hue1}, 100%, 70%, ${opacity}) 0%, 
      hsla(${hue2}, 100%, 60%, ${opacity * 0.7}) 40%, 
      hsla(${hue1}, 100%, 80%, 0) 70%
    )`;
    
    flare.style.filter = `blur(${Math.random() * 20 + 10}px)`;
    
    // Complex animation
    const duration = 5 + (Math.random() * 10);
    const delay = Math.random() * 3;
    flare.style.animation = `pulseModern ${duration}s ease-in-out ${delay}s infinite alternate`;
    
    container.appendChild(flare);
  }
  
  // Add CSS for the modern pulse animation
  if (!document.querySelector('#flare-keyframes')) {
    const style = document.createElement('style');
    style.id = 'flare-keyframes';
    style.textContent = `
      @keyframes pulseModern {
        0% { transform: translate(-50%, -50%) scale(1); opacity: 0.2; filter: blur(10px) brightness(0.8); }
        50% { transform: translate(-55%, -45%) scale(1.3); opacity: 0.3; filter: blur(15px) brightness(1.2); }
        100% { transform: translate(-45%, -55%) scale(1); opacity: 0.1; filter: blur(20px) brightness(0.9); }
      }
      .light-flare {
        position: absolute;
        border-radius: 50%;
        transform: translate(-50%, -50%);
        pointer-events: none;
        z-index: 0;
        mix-blend-mode: screen;
        will-change: transform, opacity, filter;
      }
    `;
    document.head.appendChild(style);
  }
}

// Initialize digital clock
function initClock() {
  const clockElement = document.getElementById('digital-clock');
  const dateElement = document.getElementById('current-date');
  
  if (!clockElement || !dateElement) return;
  
  // Update time function
  function updateTime() {
    const now = new Date();
    
    // Format time with leading zeros
    const hours = now.getHours().toString().padStart(2, '0');
    const minutes = now.getMinutes().toString().padStart(2, '0');
    const seconds = now.getSeconds().toString().padStart(2, '0');
    clockElement.textContent = `${hours}:${minutes}:${seconds}`;
    
    // Format date
    const options = { weekday: 'long', year: 'numeric', month: 'short', day: 'numeric' };
    dateElement.textContent = now.toLocaleDateString(undefined, options);
  }
  
  // Update immediately and then every second
  updateTime();
  setInterval(updateTime, 1000);
  
  // Add simple animation to clock when time changes
  let lastSecond = new Date().getSeconds();
  
  setInterval(() => {
    const currentSecond = new Date().getSeconds();
    if (currentSecond !== lastSecond) {
      clockElement.style.textShadow = '0 0 8px rgba(139, 92, 246, 0.5)';
      setTimeout(() => {
        clockElement.style.textShadow = 'none';
      }, 500);
      lastSecond = currentSecond;
    }
  }, 100);
}

// Initialize dynamic greeting based on time of day
function initDynamicGreeting() {
  const greetingElement = document.getElementById('greeting');
  if (!greetingElement) return;
  
  const hour = new Date().getHours();
  let greeting = "Welcome Back";
  
  // Set greeting based on time of day
  if (hour >= 5 && hour < 12) {
    greeting = "Good Morning";
  } else if (hour >= 12 && hour < 18) {
    greeting = "Good Afternoon";
  } else if (hour >= 18 && hour < 22) {
    greeting = "Good Evening";
  } else {
    greeting = "Good Night";
  }
  
  greetingElement.textContent = greeting;
}
