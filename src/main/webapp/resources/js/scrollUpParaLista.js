function initScrollOnError() {
    var observer = new MutationObserver(function(mutations) {
        mutations.forEach(function(mutation) {
            mutation.addedNodes.forEach(function(node) {
                if (node.nodeType === Node.ELEMENT_NODE) {
                    // Verifica se o próprio nó é uma mensagem
                    if (
                        node.classList.contains('ui-messages') ||
                        node.classList.contains('ui-growl') ||
                        node.className.indexOf('message') !== -1 ||
                        node.className.indexOf('error') !== -1
                    ) {
                        window.scrollTo({ top: 0, behavior: 'smooth' });
                    }
                }
            });
        });
    });

    // Observa apenas adições diretas ao body (ou ao container das mensagens, se houver)
    observer.observe(document.body, {
        childList: true,
        subtree: true
    });
}

document.addEventListener('DOMContentLoaded', initScrollOnError);
