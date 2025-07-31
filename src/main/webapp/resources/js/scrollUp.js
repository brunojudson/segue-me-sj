function initScrollOnError() {
    // Observer para detectar quando mensagens de erro são adicionadas ao DOM
    var observer = new MutationObserver(function(mutations) {
        mutations.forEach(function(mutation) {
            if (mutation.type === 'childList' && mutation.addedNodes.length > 0) {
                for (var i = 0; i < mutation.addedNodes.length; i++) {
                    var node = mutation.addedNodes[i];
                    if (node.nodeType === Node.ELEMENT_NODE) {
                        // Procura especificamente mensagens de erro
                        var hasError = node.querySelector('.ui-messages-error, .ui-message-error, .ui-growl-message-error, [class*="-error"], [class*="error"]');
                        if (
                            (hasError) ||
                            node.classList.contains('ui-messages-error') ||
                            node.classList.contains('ui-message-error') ||
                            node.classList.contains('ui-growl-message-error') ||
                            (node.className && node.className.indexOf('error') !== -1)
                        ) {
                            window.scrollTo({ top: 0, behavior: 'smooth' });
                            break;
                        }
                    }
                }
            }
        });
    });
    observer.observe(document.body, {
        childList: true,
        subtree: true
    });
}
document.addEventListener('DOMContentLoaded', initScrollOnError);
