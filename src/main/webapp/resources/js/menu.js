// JavaScript simplificado compatível com o CSS existente
		function toggleMenu() {
			const container = document.querySelector('.menu-container');
			const overlay = document.querySelector('.menu-overlay');
			const body = document.body;
			
			if (!container || !overlay) return;
			
			const isActive = container.classList.contains('active');
			
			if (isActive) {
				// Fecha menu
				container.classList.remove('active');
				overlay.style.display = 'none';
				body.classList.remove('menu-open');
				body.style.overflow = '';
			} else {
				// Abre menu
				container.classList.add('active');
				overlay.style.display = 'block';
				body.classList.add('menu-open');
				body.style.overflow = 'hidden';
			}
		}
		
		function closeMenu() {
			const container = document.querySelector('.menu-container');
			const overlay = document.querySelector('.menu-overlay');
			const body = document.body;
			
		   if (container && overlay) {
				container.classList.remove('active');
				overlay.style.display = 'none';
				body.classList.remove('menu-open');
				body.style.overflow = '';
			}
		}
		
		// Fecha menu ao redimensionar para desktop
		window.addEventListener('resize', function() {
			if (window.innerWidth > 768) {
				closeMenu();
			}
		});
		
		// Configura eventos quando o DOM estiver carregado
		document.addEventListener('DOMContentLoaded', function() {
			// Fecha menu ao clicar em links
			const menuLinks = document.querySelectorAll('.menu-link');
		   menuLinks.forEach(function(link) {
			   link.addEventListener('click', function() {
				   if (window.innerWidth <= 768) {
					   // Fecha menu centralizando toda a limpeza
					   closeMenu();
				   }
			   });
		   });
			
			// Fecha menu ao clicar no overlay
			const overlay = document.querySelector('.menu-overlay');
		   if (overlay) {
			   overlay.addEventListener('click', function() {
				   closeMenu();
			   });
		   }
			
			// Destaca página atual
			highlightCurrentPage();
		});
		
		// Função para destacar página atual
		function highlightCurrentPage() {
			const currentPath = window.location.pathname;
			const menuLinks = document.querySelectorAll('.menu-link');
			
			menuLinks.forEach(function(link) {
				link.classList.remove('active');
				const href = link.getAttribute('href');
				
			   if (href && currentPath.includes(href.replace(/^.*\/pages/, '/pages'))) {
					link.classList.add('active');
				}
			});
		}