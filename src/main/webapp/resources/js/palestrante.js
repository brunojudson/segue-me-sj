function alternarCamposPalestrante(tipoSelect) {
    var formId = 'formCadastroPalestrante:';
    var pessoa = document.getElementById(formId + 'pessoaIndividual');
    var casal = document.getElementById(formId + 'casal');
    if (!pessoa || !casal || !tipoSelect) return;
    var valor = tipoSelect.value;
    pessoa.disabled = true;
    casal.disabled = true;
    if (valor && valor.indexOf('INDIVIDUAL') !== -1) {
        pessoa.disabled = false;
    } else if (valor && valor.indexOf('CASAL') !== -1) {
        casal.disabled = false;
    }
}
document.addEventListener('DOMContentLoaded', function() {
    var tipo = document.getElementById('formCadastroPalestrante:tipo');
    if (tipo) alternarCamposPalestrante(tipo);
});