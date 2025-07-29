function initScrollOnError() {
    // Observer para detectar quando mensagens são adicionadas ao DOM
    var observer = new MutationObserver(function(mutations) {
        mutations.forEach(function(mutation) {
            if (mutation.type === 'childList' && mutation.addedNodes.length > 0) {
                // Verifica se algum nó adicionado contém mensagens
                for (var i = 0; i < mutation.addedNodes.length; i++) {
                    var node = mutation.addedNodes[i];
                    if (node.nodeType === Node.ELEMENT_NODE) {
                        var hasError = node.querySelector('.ui-messages, .ui-growl, [class*="message"], [class*="error"]');
                        if (hasError || node.classList.contains('ui-messages') || 
                            node.classList.contains('ui-growl') || 
                            node.className.indexOf('message') !== -1) {
                            window.scrollTo({ top: 0, behavior: 'smooth' });
                            break;
                        }
                    }
                }
            }
        });
    });
    
    // Observa mudanças no body
    observer.observe(document.body, {
        childList: true,
        subtree: true
    });
}

// Inicializa quando a página carrega
document.addEventListener('DOMContentLoaded', initScrollOnError);
