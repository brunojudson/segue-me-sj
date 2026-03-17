/**
 * Funções JavaScript para melhorar a experiência do usuário
 * Sistema Segue-me
 */

// Função para lidar com feedback de sucesso após salvar
function handleSaveComplete(args) {
    if (args && !args.validationFailed) {
        // Mostra mensagem de sucesso temporária
        showSuccessToast('Dados salvos com sucesso!');
        
        // Adiciona efeito visual de sucesso no botão
        addSuccessEffect();
        
        // Scroll para o topo da página para mostrar mensagens
        scrollToTop();
    } else if (args && args.validationFailed) {
        // Mostra mensagem de erro
        showErrorToast('Por favor, corrija os erros antes de continuar.');
        
        // Foca no primeiro campo com erro
        focusFirstError();
    }
}

// Mostra toast de sucesso
function showSuccessToast(message) {
    if (window.PF && PF('growl')) {
        PF('growl').renderMessage({
            summary: 'Sucesso',
            detail: message,
            severity: 'success'
        });
    } else {
        // Fallback caso não tenha growl configurado
        console.log('✅ ' + message);
    }
}

// Mostra toast de erro
function showErrorToast(message) {
    if (window.PF && PF('growl')) {
        PF('growl').renderMessage({
            summary: 'Erro',
            detail: message,
            severity: 'error'
        });
    } else {
        // Fallback caso não tenha growl configurado
        console.log('❌ ' + message);
    }
}

// Adiciona efeito visual de sucesso no botão
function addSuccessEffect() {
    const saveButtons = document.querySelectorAll('.action-button-primary');
    saveButtons.forEach(button => {
        if (button) {
            button.classList.add('success-effect');
            setTimeout(() => {
                button.classList.remove('success-effect');
            }, 2000);
        }
    });
}

// Foca no primeiro campo com erro
function focusFirstError() {
    setTimeout(() => {
        const errorField = document.querySelector('.ui-state-error');
        if (errorField) {
            errorField.focus();
            errorField.scrollIntoView({ 
                behavior: 'smooth', 
                block: 'center' 
            });
        }
    }, 100);
}

// Scroll suave para o topo
function scrollToTop() {
    try {
        var content = document.getElementById('content');
        if (content) {
            // Tenta alinhar a primeira seção de conteúdo ao topo, compensando padding
            var target = content.querySelector('.content-container') || content.firstElementChild;
            var paddingTop = parseFloat(getComputedStyle(content).paddingTop) || 0;
            // compensa a altura do header (definida em --header-h no template)
            var headerH = parseFloat(getComputedStyle(document.documentElement).getPropertyValue('--header-h')) || 0;
            var targetOffset = 0;
            if (target) {
                targetOffset = target.offsetTop - paddingTop - headerH;
                if (targetOffset < 0) targetOffset = 0;
            }
            if (typeof content.scrollTo === 'function') {
                content.scrollTo({ top: targetOffset, behavior: 'smooth' });
            } else {
                content.scrollTop = targetOffset;
            }
            return;
        }
    } catch (e) { }
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

// Inicialização quando o DOM estiver pronto
document.addEventListener('DOMContentLoaded', function() {
    
    // Adiciona animações aos fieldsets
    initFieldsetAnimations();
    
    // Melhora a acessibilidade
    improveAccessibility();
    
    // Adiciona tooltips dinâmicos
    addDynamicTooltips();
    
    // Inicializa validação em tempo real
    initRealTimeValidation();
    
    // Inicializa o modo escuro baseado na preferência salva
    initDarkMode();
    
    // Animação de entrada do header
    initHeaderAnimations();
    // Inicializa comportamento do menu (toggle, overlay, links)
    try { initMenu(); } catch (e) { /* ignore */ }

    // Garante que ao carregar uma nova página o conteúdo comece no topo
    try { scrollToTop(); } catch (e) { /* ignore */ }
});

// Inicializa animações nos fieldsets
function initFieldsetAnimations() {
    const fieldsets = document.querySelectorAll('.modern-fieldset');
    fieldsets.forEach(fieldset => {
        fieldset.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-2px)';
        });
        
        fieldset.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0)';
        });
    });
}

// Melhora a acessibilidade
function improveAccessibility() {
    // Adiciona navegação por teclado nos botões de ação
    const actionButtons = document.querySelectorAll('.action-buttons .ui-button');
    actionButtons.forEach((button, index) => {
        button.setAttribute('tabindex', index + 1);
    });
    
    // Adiciona aria-labels úteis
    const requiredFields = document.querySelectorAll('.required-field');
    requiredFields.forEach(field => {
        const input = field.parentElement.querySelector('input, select, textarea');
        if (input) {
            input.setAttribute('aria-required', 'true');
        }
    });
}

// Adiciona tooltips dinâmicos
function addDynamicTooltips() {
    const hints = document.querySelectorAll('.field-hint');
    hints.forEach(hint => {
        const fieldGroup = hint.closest('.field-group');
        const input = fieldGroup ? fieldGroup.querySelector('input, select, textarea') : null;
        
        if (input && hint.textContent.trim()) {
            input.setAttribute('title', hint.textContent.trim());
        }
    });
}

// Inicializa validação em tempo real
function initRealTimeValidation() {
    const inputs = document.querySelectorAll('input[required], select[required], textarea[required]');
    
    inputs.forEach(input => {
        input.addEventListener('blur', function() {
            validateField(this);
        });
        
        input.addEventListener('input', function() {
            // Remove o erro quando o usuário começa a digitar
            if (this.classList.contains('ui-state-error')) {
                this.classList.remove('ui-state-error');
            }
        });
    });
}

// Valida um campo individual
function validateField(field) {
    if (field.hasAttribute('required') && !field.value.trim()) {
        field.classList.add('ui-state-error');
        
        // Adiciona shake effect
        field.style.animation = 'shake 0.5s';
        setTimeout(() => {
            field.style.animation = '';
        }, 500);
    } else {
        field.classList.remove('ui-state-error');
    }
}

// Função para confirmar ações perigosas
function confirmDangerousAction(message, callback) {
    const confirmed = confirm(message || 'Tem certeza que deseja executar esta ação?');
    if (confirmed && typeof callback === 'function') {
        callback();
    }
    return confirmed;
}

// Função para formatar inputs de telefone automaticamente
function formatPhoneInput(input) {
    let value = input.value.replace(/\D/g, '');
    
    if (value.length <= 10) {
        value = value.replace(/(\d{2})(\d{4})(\d{4})/, '($1) $2-$3');
    } else {
        value = value.replace(/(\d{2})(\d{5})(\d{4})/, '($1) $2-$3');
    }
    
    input.value = value;
}

// Auto-aplicar formatação em campos de telefone
document.addEventListener('DOMContentLoaded', function() {
    const phoneInputs = document.querySelectorAll('input[id*="telefone"], input[id*="phone"]');
    phoneInputs.forEach(input => {
        input.addEventListener('input', function() {
            formatPhoneInput(this);
        });
    });
});

/**
 * Função para alternar modo escuro
 */
function toggleDarkMode() {
    const body = document.body;
    const isDarkMode = body.classList.contains('dark-mode');
    
    if (isDarkMode) {
        body.classList.remove('dark-mode');
        localStorage.setItem('darkMode', 'false');
        
        // Atualiza ícone do botão
        const button = document.querySelector('.dark-mode-btn i');
        if (button) {
            button.className = 'pi pi-moon';
        }
    } else {
        body.classList.add('dark-mode');
        localStorage.setItem('darkMode', 'true');
        
        // Atualiza ícone do botão
        const button = document.querySelector('.dark-mode-btn i');
        if (button) {
            button.className = 'pi pi-sun';
        }
    }
}

/**
 * Inicializa o modo escuro baseado na preferência salva
 */
function initDarkMode() {
    const darkMode = localStorage.getItem('darkMode');
    const prefersDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
    
    if (darkMode === 'true' || (darkMode === null && prefersDark)) {
        document.body.classList.add('dark-mode');
        
        // Atualiza ícone do botão
        const button = document.querySelector('.dark-mode-btn i');
        if (button) {
            button.className = 'pi pi-sun';
        }
    }
}

/**
 * Animações para o header
 */
function initHeaderAnimations() {
    // Efeito ripple nos botões do header
    const headerButtons = document.querySelectorAll('.header-btn');
    headerButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            const ripple = document.createElement('span');
            const rect = this.getBoundingClientRect();
            const size = Math.max(rect.width, rect.height);
            const x = e.clientX - rect.left - size / 2;
            const y = e.clientY - rect.top - size / 2;
            
            ripple.style.width = ripple.style.height = size + 'px';
            ripple.style.left = x + 'px';
            ripple.style.top = y + 'px';
            ripple.classList.add('ripple');
            
            this.appendChild(ripple);
            
            setTimeout(() => {
                ripple.remove();
            }, 600);
        });
    });
}

// CSS para efeito ripple
const rippleCSS = `
.header-btn {
    position: relative;
    overflow: hidden;
}

.ripple {
    position: absolute;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.6);
    transform: scale(0);
    animation: rippleEffect 0.6s linear;
    pointer-events: none;
}

@keyframes rippleEffect {
    to {
        transform: scale(4);
        opacity: 0;
    }
}
`;

// Adiciona CSS do ripple
const style = document.createElement('style');
style.textContent = rippleCSS;
document.head.appendChild(style);

// Adiciona estilos CSS inline para animações
const style2 = document.createElement('style');
style2.textContent = `
    @keyframes shake {
        0%, 100% { transform: translateX(0); }
        25% { transform: translateX(-5px); }
        75% { transform: translateX(5px); }
    }
    
    .success-effect {
        animation: successPulse 2s ease-in-out;
    }
    
    @keyframes successPulse {
        0% { transform: scale(1); }
        50% { transform: scale(1.05); box-shadow: 0 0 20px rgba(16, 185, 129, 0.6); }
        100% { transform: scale(1); }
    }
    
    .modern-fieldset {
        transition: transform 0.3s ease, box-shadow 0.3s ease;
    }
    
    .loading-overlay {
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(255, 255, 255, 0.8);
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 9999;
        backdrop-filter: blur(2px);
    }
    
    .loading-spinner {
        width: 50px;
        height: 50px;
        border: 4px solid #f3f3f3;
        border-top: 4px solid #3b82f6;
        border-radius: 50%;
        animation: spin 1s linear infinite;
    }
    
    @keyframes spin {
        0% { transform: rotate(0deg); }
        100% { transform: rotate(360deg); }
    }
`;
document.head.appendChild(style2);

// Adiciona loading overlay para operações demoradas
function showLoadingOverlay(message = 'Carregando...') {
    const overlay = document.createElement('div');
    overlay.className = 'loading-overlay';
    overlay.id = 'loadingOverlay';
    
    overlay.innerHTML = `
        <div style="display: flex; flex-direction: column; align-items: center; gap: 16px;">
            <div class="loading-spinner"></div>
            <span style="color: #374151; font-weight: 500;">${message}</span>
        </div>
    `;
    
    document.body.appendChild(overlay);
}

function hideLoadingOverlay() {
    const overlay = document.getElementById('loadingOverlay');
    if (overlay) {
        overlay.remove();
    }
}

// Expor funções globalmente
window.handleSaveComplete = handleSaveComplete;
window.showSuccessToast = showSuccessToast;
window.showErrorToast = showErrorToast;
window.confirmDangerousAction = confirmDangerousAction;
window.showLoadingOverlay = showLoadingOverlay;
window.hideLoadingOverlay = hideLoadingOverlay;
window.toggleDarkMode = toggleDarkMode;
window.initDarkMode = initDarkMode;
window.initHeaderAnimations = initHeaderAnimations;

// Menu mobile: funções e inicialização centralizada
function toggleMenu() {
    var menu = document.getElementById('menu');
    var menuContainer = menu ? menu.querySelector('.menu-container') : null;
    var overlay = document.querySelector('.menu-overlay');
    var body = document.body;
    if (!menuContainer || !overlay) return;
    var isOpen = menuContainer.classList.contains('active');
    if (isOpen) {
        console.debug('[menu] toggleMenu: closing');
        menuContainer.classList.remove('active');
        body.classList.remove('menu-open');
        body.style.overflow = '';
    } else {
        console.debug('[menu] toggleMenu: opening');
        menuContainer.classList.add('active');
        body.classList.add('menu-open');
        body.style.overflow = 'hidden';
    }
}

function closeMenu() {
    var menu = document.getElementById('menu');
    var menuContainer = menu ? menu.querySelector('.menu-container') : null;
    var overlay = document.querySelector('.menu-overlay');
    var body = document.body;
    if (menuContainer) menuContainer.classList.remove('active');
    body.classList.remove('menu-open');
    body.style.overflow = '';
}

// Handler para cliques em links do menu (fecha em mobile)
function _menuLinkHandler(e) {
    var link = e.target.closest && e.target.closest('.menu-link');
    if (link && window.innerWidth <= 768) {
        // Delay curto para permitir navegação por link tradicional
        setTimeout(closeMenu, 50);
    }
}

function initMenu() {
    // attach toggle and overlay listeners
    var toggle = document.querySelector('.menu-toggle');
    if (toggle) {
        toggle.removeEventListener('click', toggleMenu);
        toggle.addEventListener('click', toggleMenu);
    }
    var overlay = document.querySelector('.menu-overlay');
    // listeners no overlay (visibilidade controlada por CSS via `body.menu-open`)
    if (overlay) {
        overlay.removeEventListener('click', closeMenu);
        overlay.addEventListener('click', closeMenu);
    }

    // use event delegation on body for menu link clicks
    document.body.removeEventListener('click', _menuLinkHandler);
    document.body.addEventListener('click', _menuLinkHandler);

    // calcula --header-h para compensar scrollToTop
    try {
        var header = document.getElementById('header');
        if (header) {
            document.documentElement.style.setProperty('--header-h', header.offsetHeight + 'px');
        }
    } catch (e) { }

    // Garantir estado fechado ao inicializar para evitar overlay ativo indevido
    try {
        var menu = document.getElementById('menu');
        var container = menu ? menu.querySelector('.menu-container') : null;
        var overlayEl = document.querySelector('.menu-overlay');
        if (container) container.classList.remove('active');
        if (overlayEl) { overlayEl.style.pointerEvents = 'none'; }
        document.body.classList.remove('menu-open');
        document.body.style.overflow = '';
        console.debug('[menu] initMenu state:', { bodyClass: document.body.className, containerClass: container ? container.className : null, overlayDisplay: overlayEl ? overlayEl.style.display : null });
    } catch (e) { }
}

// Re-inicializa menu após callbacks AJAX do PrimeFaces (compatível com várias versões)
(function() {
    function _onAjaxEvent(data) {
        try {
            if (!data || data.status === 'success' || data.status === 'complete') {
                initMenu();
            }
        } catch (e) { }
    }

    if (window.PrimeFaces && PrimeFaces.ajax) {
        try {
            if (PrimeFaces.ajax.Request && typeof PrimeFaces.ajax.Request.addOnEventHandler === 'function') {
                PrimeFaces.ajax.Request.addOnEventHandler(_onAjaxEvent);
            } else if (typeof PrimeFaces.ajax.addOnEventHandler === 'function') {
                PrimeFaces.ajax.addOnEventHandler(_onAjaxEvent);
            } else {
                // fallback: escuta eventos personalizados do PrimeFaces
                document.addEventListener('pfAjaxComplete', function() { _onAjaxEvent(); }, false);
                if (window.jQuery) jQuery(document).on('pfAjaxComplete', function() { _onAjaxEvent(); });
            }
        } catch (e) {
            // se qualquer coisa falhar, usa fallback de evento
            document.addEventListener('pfAjaxComplete', function() { _onAjaxEvent(); }, false);
            if (window.jQuery) jQuery(document).on('pfAjaxComplete', function() { _onAjaxEvent(); });
        }
    } else {
        // PrimeFaces não definido ainda — usa fallback nos eventos do documento/jQuery
        document.addEventListener('pfAjaxComplete', function() { _onAjaxEvent(); }, false);
        if (window.jQuery) jQuery(document).on('pfAjaxComplete', function() { _onAjaxEvent(); });
    }
})();

// Tornar funções acessíveis globalmente (compatibilidade)
window.toggleMenu = toggleMenu;
window.closeMenu = closeMenu;
window.initMenu = initMenu;

// Busca rápida no menu (usada no onkeyup do input de busca)
function filterMenu(query) {
    var normalizedQuery = (query || '').toLowerCase().normalize('NFD').replace(/[{\u0300-\u036f}]/g, '').replace(/[\u0300-\u036f]/g, '');
    var menuItems = document.querySelectorAll('.main-menu > li');
    var noResults = document.getElementById('menuNoResults');
    var hasVisible = false;
    menuItems.forEach(function(section) {
        var links = section.querySelectorAll('.submenu li');
        var sectionHasMatch = false;
        links.forEach(function(item) {
            var text = (item.textContent || '').toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '');
            if (!normalizedQuery || text.indexOf(normalizedQuery) !== -1) {
                item.style.display = '';
                sectionHasMatch = true;
            } else {
                item.style.display = 'none';
            }
        });
        section.style.display = sectionHasMatch ? '' : 'none';
        if (sectionHasMatch) hasVisible = true;
    });
    if (noResults) {
        noResults.style.display = (normalizedQuery && !hasVisible) ? 'flex' : 'none';
    }
}

// Fecha menu ao redimensionar para desktop
window.addEventListener('resize', function() {
    if (window.innerWidth > 768) {
        closeMenu();
    }
});

/* ========================================
   LOADING INDICATORS PARA DATATABLES
======================================== */

// Adiciona loading indicator nas dataTables durante filtros e paginação
function initDataTableLoadingIndicators() {
    // Intercepta eventos AJAX do PrimeFaces (compatível com várias versões)
    if (typeof PrimeFaces !== 'undefined') {
        var _dtHandler = function(data) {
            try {
                var source = data && data.source;
                if (source && source.closest) {
                    var table = source.closest('.ui-datatable');
                    if (table && data.status === 'begin') {
                        showDataTableLoading(table);
                        return;
                    }
                    if (table && (data.status === 'complete' || data.status === 'success')) {
                        hideDataTableLoading(table);
                        return;
                    }
                }
            } catch (e) { }
        };

        try {
            if (PrimeFaces.ajax && PrimeFaces.ajax.Request && typeof PrimeFaces.ajax.Request.addOnEventHandler === 'function') {
                PrimeFaces.ajax.Request.addOnEventHandler(_dtHandler);
            } else if (PrimeFaces.ajax && typeof PrimeFaces.ajax.addOnEventHandler === 'function') {
                PrimeFaces.ajax.addOnEventHandler(_dtHandler);
            } else {
                // fallback: usa eventos globais pfAjaxStart/pfAjaxComplete
                document.addEventListener('pfAjaxStart', function() {
                    document.querySelectorAll('.ui-datatable').forEach(function(table) { showDataTableLoading(table); });
                });
                document.addEventListener('pfAjaxComplete', function() {
                    document.querySelectorAll('.ui-datatable').forEach(function(table) { hideDataTableLoading(table); });
                });
                if (window.jQuery) {
                    jQuery(document).on('pfAjaxStart', function() { jQuery('.ui-datatable').each(function() { showDataTableLoading(this); }); });
                    jQuery(document).on('pfAjaxComplete', function() { jQuery('.ui-datatable').each(function() { hideDataTableLoading(this); }); });
                }
            }
        } catch (e) {
            // último recurso: exibir/hide em eventos pfAjaxComplete (sem contexto)
            document.addEventListener('pfAjaxStart', function() { document.querySelectorAll('.ui-datatable').forEach(function(table) { showDataTableLoading(table); }); });
            document.addEventListener('pfAjaxComplete', function() { document.querySelectorAll('.ui-datatable').forEach(function(table) { hideDataTableLoading(table); }); });
        }
    }
    
    // Observer para detectar quando novas dataTables são adicionadas ao DOM
    const observer = new MutationObserver(function(mutations) {
        mutations.forEach(function(mutation) {
            mutation.addedNodes.forEach(function(node) {
                if (node.nodeType === 1 && node.classList && node.classList.contains('ui-datatable')) {
                    setupDataTableLoading(node);
                }
            });
        });
    });
    
    // Inicia observação do body
    observer.observe(document.body, {
        childList: true,
        subtree: true
    });
    
    // Configura dataTables existentes
    document.querySelectorAll('.ui-datatable').forEach(setupDataTableLoading);
}

// Configura uma dataTable específica para loading indicators
function setupDataTableLoading(table) {
    if (table.dataset.loadingConfigured) return;
    table.dataset.loadingConfigured = 'true';
    
    // Adiciona listeners para filtros
    const filterInputs = table.querySelectorAll('.ui-column-filter');
    filterInputs.forEach(input => {
        input.addEventListener('input', debounce(function() {
            showDataTableLoading(table);
        }, 100));
    });
    
    // Adiciona listeners para paginação
    const paginators = table.querySelectorAll('.ui-paginator a, .ui-paginator select');
    paginators.forEach(element => {
        element.addEventListener('click', function() {
            showDataTableLoading(table);
        });
        if (element.tagName === 'SELECT') {
            element.addEventListener('change', function() {
                showDataTableLoading(table);
            });
        }
    });
}

// Mostra loading indicator em uma dataTable
function showDataTableLoading(table) {
    if (!table) return;
    
    // Verifica se já existe overlay
    let overlay = table.querySelector('.ui-datatable-loading-overlay');
    if (overlay) return;
    
    // Cria overlay
    overlay = document.createElement('div');
    overlay.className = 'ui-datatable-loading-overlay';
    overlay.innerHTML = `
        <div class="ui-datatable-loading-content">
            <i class="pi pi-spin pi-spinner" aria-hidden="true"></i>
            <span>Carregando...</span>
        </div>
    `;
    
    // Adiciona ao container da tabela
    const tableWrapper = table.querySelector('.ui-datatable-tablewrapper') || table;
    tableWrapper.style.position = 'relative';
    tableWrapper.appendChild(overlay);
}

// Remove loading indicator de uma dataTable
function hideDataTableLoading(table) {
    if (!table) return;
    
    const overlay = table.querySelector('.ui-datatable-loading-overlay');
    if (overlay) {
        overlay.remove();
    }
}

// Utility: debounce function
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// Inicializa loading indicators quando o DOM estiver pronto
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initDataTableLoadingIndicators);
} else {
    initDataTableLoadingIndicators();
}

// Expõe funções de dataTable globalmente
window.initDataTableLoadingIndicators = initDataTableLoadingIndicators;
window.showDataTableLoading = showDataTableLoading;
window.hideDataTableLoading = hideDataTableLoading;
