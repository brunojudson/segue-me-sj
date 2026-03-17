# MÓDULO DE VENDAS - Páginas JSF/PrimeFaces
## Exemplos Completos de Páginas XHTML com Atalhos de Teclado

---

## 1. PÁGINA PRINCIPAL: Gestão de Vendas

**Arquivo**: `pages/venda/gestao-vendas.xhtml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:h="http://xmlns.jcp.org/jsf/html"
      xmlns:f="http://xmlns.jcp.org/jsf/core"
      xmlns:ui="http://xmlns.jcp.org/jsf/facelets"
      xmlns:p="http://primefaces.org/ui">

<ui:composition template="/templates/template.xhtml">
    <ui:define name="title">Gestão de Vendas</ui:define>
    
    <ui:define name="content">
        <h:form id="formGestaoVendas" styleClass="modern-form">
            
            <!-- Mensagens de feedback -->
            <p:growl id="growl" showDetail="true" sticky="false" life="5000">
                <p:autoUpdate />
            </p:growl>
            
            <!-- Fieldset: Seleção de Encontro -->
            <p:fieldset legend="Encontro Ativo" toggleable="false" styleClass="modern-fieldset">
                <div class="field-grid">
                    <div class="field-group">
                        <label for="encontro">Encontro:</label>
                        <p:selectOneMenu id="encontro" value="#{vendaPedidoController.encontroSelecionadoId}" 
                                         required="true" requiredMessage="Selecione um encontro">
                            <f:selectItem itemLabel="-- Selecione --" itemValue="#{null}" noSelectionOption="true"/>
                            <f:selectItems value="#{vendaPedidoController.encontrosAtivos}" 
                                           var="enc" itemLabel="#{enc.nome}" itemValue="#{enc.id}"/>
                            <p:ajax listener="#{vendaPedidoController.onEncontroChanged}" 
                                    update="pnlNovaVenda pnlContasAbertas" />
                        </p:selectOneMenu>
                    </div>
                    
                    <div class="field-group">
                        <p:outputLabel value="Status do Encontro:" styleClass="label-info" />
                        <p:outputLabel value="#{vendaPedidoController.encontroAtivo ? '✓ ATIVO' : '✗ Inativo'}" 
                                       styleClass="#{vendaPedidoController.encontroAtivo ? 'status-active' : 'status-inactive'}" />
                    </div>
                </div>
                
                <p:messages id="mensagensEncontro" for="encontro" showDetail="true" closable="true" />
            </p:fieldset>
            
            <!-- Fieldset: Contas Abertas -->
            <p:fieldset id="pnlContasAbertas" legend="Contas Abertas" toggleable="true" collapsed="false" 
                        styleClass="modern-fieldset" rendered="#{vendaPedidoController.encontroSelecionadoId != null}">
                
                <p:dataTable id="tblContasAbertas" value="#{vendaPedidoController.pedidosAbertos}" 
                             var="pedido" emptyMessage="Nenhuma conta aberta"
                             paginator="true" rows="10" paginatorPosition="bottom"
                             styleClass="modern-datatable" selectionMode="single"
                             selection="#{vendaPedidoController.pedidoAtual}" rowKey="#{pedido.id}">
                    
                    <p:ajax event="rowSelect" listener="#{vendaPedidoController.selecionarPedido}" 
                            update="pnlNovaVenda pnlItens" />
                    
                    <p:column headerText="Número" width="120">
                        <h:outputText value="#{pedido.numeroPedido}" />
                    </p:column>
                    
                    <p:column headerText="Trabalhador" sortBy="#{pedido.trabalhadorResponsavel.pessoa.nome}">
                        <h:outputText value="#{pedido.trabalhadorResponsavel.pessoa.nome}" />
                    </p:column>
                    
                    <p:column headerText="Data Abertura" width="140">
                        <h:outputText value="#{pedido.dataAbertura}">
                            <f:convertDateTime pattern="dd/MM/yyyy HH:mm" />
                        </h:outputText>
                    </p:column>
                    
                    <p:column headerText="Itens" width="80" style="text-align:center;">
                        <h:outputText value="#{pedido.quantidadeTotalItens}" />
                    </p:column>
                    
                    <p:column headerText="Valor Total" width="120" style="text-align:right;">
                        <h:outputText value="#{pedido.valorTotal}">
                            <f:convertNumber type="currency" currencySymbol="R$ " />
                        </h:outputText>
                    </p:column>
                    
                    <p:column headerText="Status" width="100">
                        <p:tag value="#{pedido.status.descricao}" severity="info" />
                    </p:column>
                    
                    <p:column headerText="Ações" width="150" style="text-align:center;">
                        <p:commandButton icon="pi pi-pencil" title="Editar" 
                                         action="#{vendaPedidoController.selecionarPedido(pedido)}"
                                         update="pnlNovaVenda pnlItens" styleClass="action-button-edit" />
                        
                        <p:commandButton icon="pi pi-times" title="Cancelar Pedido" 
                                         action="#{vendaPedidoController.cancelarPedido(pedido.id)}"
                                         update="@form" styleClass="action-button-delete">
                            <p:confirm header="Confirmação" message="Deseja realmente cancelar este pedido?" 
                                       icon="pi pi-exclamation-triangle" />
                        </p:commandButton>
                    </p:column>
                </p:dataTable>
                
                <p:confirmDialog global="true" showEffect="fade" hideEffect="fade" responsive="true" width="350">
                    <p:commandButton value="Não" type="button" styleClass="ui-confirmdialog-no" icon="pi pi-times" />
                    <p:commandButton value="Sim" type="button" styleClass="ui-confirmdialog-yes" icon="pi pi-check" />
                </p:confirmDialog>
            </p:fieldset>
            
            <!-- Fieldset: Nova Venda / Adicionar Itens -->
            <p:fieldset id="pnlNovaVenda" legend="#{vendaPedidoController.pedidoAtual == null ? 'Nova Venda' : 'Adicionar Itens ao Pedido'}" 
                        toggleable="true" collapsed="false" styleClass="modern-fieldset"
                        rendered="#{vendaPedidoController.encontroSelecionadoId != null}">
                
                <!-- Seção: Iniciar Nova Venda -->
                <h:panelGroup id="pnlIniciarVenda" rendered="#{vendaPedidoController.pedidoAtual == null}">
                    <div class="field-grid">
                        <div class="field-group">
                            <label for="trabalhador">Trabalhador Responsável: *</label>
                            <p:selectOneMenu id="trabalhador" value="#{vendaPedidoController.trabalhadorSelecionadoId}" 
                                             filter="true" filterMatchMode="contains" required="true">
                                <f:selectItem itemLabel="-- Selecione --" itemValue="#{null}" noSelectionOption="true"/>
                                <f:selectItems value="#{vendaPedidoController.trabalhadores}" 
                                               var="trab" itemLabel="#{trab.pessoa.nome}" itemValue="#{trab.id}"/>
                            </p:selectOneMenu>
                        </div>
                    </div>
                    
                    <div class="form-buttons">
                        <p:commandButton value="Nova Venda (Alt+N)" icon="pi pi-plus" 
                                         actionListener="#{vendaPedidoController.iniciarNovaVenda}"
                                         update="@form" styleClass="action-button-primary"
                                         accesskey="N" />
                    </div>
                </h:panelGroup>
                
                <!-- Seção: Adicionar Item a Pedido Existente -->
                <h:panelGroup id="pnlAdicionarItem" rendered="#{vendaPedidoController.pedidoAtual != null}">
                    <p:panel header="Pedido: #{vendaPedidoController.pedidoAtual.numeroPedido} - Trabalhador: #{vendaPedidoController.pedidoAtual.trabalhadorResponsavel.pessoa.nome}" 
                             styleClass="info-panel">
                        <div class="field-grid">
                            <div class="field-group">
                                <label for="artigo">Artigo: *</label>
                                <p:selectOneMenu id="artigo" value="#{vendaPedidoController.artigoSelecionadoId}" 
                                                 filter="true" filterMatchMode="contains" required="true">
                                    <f:selectItem itemLabel="-- Selecione --" itemValue="#{null}" noSelectionOption="true"/>
                                    <f:selectItems value="#{vendaPedidoController.artigosAtivos}" 
                                                   var="art" itemLabel="#{art.nome} - R$ #{art.precoBase}" itemValue="#{art.id}"/>
                                    <p:ajax listener="#{vendaPedidoController.onArtigoSelecionado}" 
                                            update="valorUnitario" />
                                </p:selectOneMenu>
                            </div>
                            
                            <div class="field-group">
                                <label for="quantidade">Quantidade: *</label>
                                <p:inputNumber id="quantidade" value="#{vendaPedidoController.quantidadeItem}" 
                                               decimalPlaces="0" minValue="1" required="true" />
                            </div>
                            
                            <div class="field-group">
                                <label for="valorUnitario">Valor Unitário: *</label>
                                <p:inputNumber id="valorUnitario" value="#{vendaPedidoController.valorUnitarioItem}" 
                                               mode="currency" currencySymbol="R$ " required="true" />
                            </div>
                        </div>
                        
                        <div class="form-buttons">
                            <p:commandButton value="Adicionar Item (Alt+I)" icon="pi pi-plus-circle" 
                                             actionListener="#{vendaPedidoController.adicionarItem}"
                                             update="@form" styleClass="action-button-success"
                                             accesskey="I" />
                            
                            <p:commandButton value="Cancelar / Nova Venda" icon="pi pi-times" 
                                             actionListener="#{vendaPedidoController.limpar}"
                                             update="@form" styleClass="action-button-secondary" />
                        </div>
                    </p:panel>
                </h:panelGroup>
            </p:fieldset>
            
            <!-- Fieldset: Itens do Pedido Atual -->
            <p:fieldset id="pnlItens" legend="Itens do Pedido" toggleable="true" collapsed="false" 
                        styleClass="modern-fieldset"
                        rendered="#{vendaPedidoController.pedidoAtual != null and not empty vendaPedidoController.pedidoAtual.itens}">
                
                <p:dataTable id="tblItens" value="#{vendaPedidoController.pedidoAtual.itens}" 
                             var="item" emptyMessage="Nenhum item adicionado"
                             styleClass="modern-datatable">
                    
                    <p:column headerText="Artigo">
                        <h:outputText value="#{item.artigo.nome}" />
                    </p:column>
                    
                    <p:column headerText="Categoria" width="150">
                        <h:outputText value="#{item.artigo.categoria}" />
                    </p:column>
                    
                    <p:column headerText="Quantidade" width="100" style="text-align:center;">
                        <h:outputText value="#{item.quantidade}" />
                    </p:column>
                    
                    <p:column headerText="Valor Unit." width="120" style="text-align:right;">
                        <h:outputText value="#{item.valorUnitario}">
                            <f:convertNumber type="currency" currencySymbol="R$ " />
                        </h:outputText>
                    </p:column>
                    
                    <p:column headerText="Valor Total" width="120" style="text-align:right;">
                        <h:outputText value="#{item.valorTotalItem}">
                            <f:convertNumber type="currency" currencySymbol="R$ " />
                        </h:outputText>
                    </p:column>
                    
                    <p:column headerText="Ação" width="80" style="text-align:center;">
                        <p:commandButton icon="pi pi-trash" title="Remover" 
                                         actionListener="#{vendaPedidoController.removerItem(item.id)}"
                                         update="@form" styleClass="action-button-delete"
                                         disabled="#{!vendaPedidoController.pedidoAtual.status.permiteAlteracao()}">
                            <p:confirm header="Confirmação" message="Deseja remover este item?" 
                                       icon="pi pi-exclamation-triangle" />
                        </p:commandButton>
                    </p:column>
                    
                    <f:facet name="footer">
                        <div style="text-align:right; font-weight:bold; font-size:1.1em;">
                            Total do Pedido: 
                            <h:outputText value="#{vendaPedidoController.pedidoAtual.valorTotal}">
                                <f:convertNumber type="currency" currencySymbol="R$ " />
                            </h:outputText>
                        </div>
                    </f:facet>
                </p:dataTable>
                
                <!-- Botões de Ação Principal -->
                <div class="form-buttons" style="margin-top: 1rem;">
                    <p:commandButton value="Fechar Pedido (Alt+F)" icon="pi pi-check-circle" 
                                     onclick="PF('dlgFecharPedido').show(); return false;"
                                     styleClass="action-button-primary" accesskey="F"
                                     disabled="#{empty vendaPedidoController.pedidoAtual.itens}" />
                    
                    <p:commandButton value="Adicionar Mais Itens (Alt+I)" icon="pi pi-plus" 
                                     onclick="document.getElementById('formGestaoVendas:artigo_input').focus(); return false;"
                                     styleClass="action-button-success" accesskey="I" />
                </div>
            </p:fieldset>
            
            <!-- Dialog: Fechar Pedido -->
            <p:dialog id="dlgFecharPedido" header="Fechar Pedido" widgetVar="dlgFecharPedido" 
                      modal="true" responsive="true" width="500" closeOnEscape="true">
                
                <div class="dialog-content">
                    <p:panelGrid columns="2" layout="grid" styleClass="modern-panel-grid">
                        <p:outputLabel value="Número do Pedido:" styleClass="label-bold" />
                        <p:outputLabel value="#{vendaPedidoController.pedidoAtual.numeroPedido}" />
                        
                        <p:outputLabel value="Trabalhador:" styleClass="label-bold" />
                        <p:outputLabel value="#{vendaPedidoController.pedidoAtual.trabalhadorResponsavel.pessoa.nome}" />
                        
                        <p:outputLabel value="Valor Total:" styleClass="label-bold" />
                        <h:outputText value="#{vendaPedidoController.pedidoAtual.valorTotal}">
                            <f:convertNumber type="currency" currencySymbol="R$ " />
                        </h:outputText>
                    </p:panelGrid>
                    
                    <p:separator />
                    
                    <div class="field-grid">
                        <div class="field-group">
                            <p:selectBooleanCheckbox id="pagarAgora" value="#{vendaPedidoController.pagarAgora}">
                                <p:ajax update="pnlPagamento" />
                            </p:selectBooleanCheckbox>
                            <label for="pagarAgora" style="margin-left: 0.5rem;">Pagar Agora</label>
                        </div>
                    </div>
                    
                    <h:panelGroup id="pnlPagamento" rendered="#{vendaPedidoController.pagarAgora}">
                        <div class="field-grid">
                            <div class="field-group">
                                <label for="formaPagamento">Forma de Pagamento: *</label>
                                <p:selectOneMenu id="formaPagamento" value="#{vendaPedidoController.formaPagamento}" required="true">
                                    <f:selectItem itemLabel="-- Selecione --" itemValue="#{null}" />
                                    <f:selectItem itemLabel="Dinheiro" itemValue="DINHEIRO" />
                                    <f:selectItem itemLabel="PIX" itemValue="PIX" />
                                    <f:selectItem itemLabel="Cartão de Débito" itemValue="DEBITO" />
                                    <f:selectItem itemLabel="Cartão de Crédito" itemValue="CREDITO" />
                                </p:selectOneMenu>
                            </div>
                        </div>
                    </h:panelGroup>
                </div>
                
                <f:facet name="footer">
                    <p:commandButton value="Confirmar" icon="pi pi-check" 
                                     actionListener="#{vendaPedidoController.fecharPedido}"
                                     update="@form" oncomplete="PF('dlgFecharPedido').hide();"
                                     styleClass="action-button-primary" />
                    
                    <p:commandButton value="Cancelar" icon="pi pi-times" 
                                     onclick="PF('dlgFecharPedido').hide(); return false;"
                                     styleClass="action-button-secondary" />
                </f:facet>
            </p:dialog>
            
            <!-- Fieldset: Pedidos Aguardando Pagamento -->
            <p:fieldset legend="Pedidos Aguardando Pagamento" toggleable="true" collapsed="true" 
                        styleClass="modern-fieldset"
                        rendered="#{vendaPedidoController.encontroSelecionadoId != null and not empty vendaPedidoController.pedidosAguardandoPagamento}">
                
                <p:dataTable id="tblAguardandoPagamento" value="#{vendaPedidoController.pedidosAguardandoPagamento}" 
                             var="pedido" emptyMessage="Nenhum pedido aguardando pagamento"
                             styleClass="modern-datatable">
                    
                    <p:column headerText="Número" width="120">
                        <h:outputText value="#{pedido.numeroPedido}" />
                    </p:column>
                    
                    <p:column headerText="Trabalhador">
                        <h:outputText value="#{pedido.trabalhadorResponsavel.pessoa.nome}" />
                    </p:column>
                    
                    <p:column headerText="Data Fechamento" width="140">
                        <h:outputText value="#{pedido.dataFechamento}">
                            <f:convertDateTime pattern="dd/MM/yyyy HH:mm" />
                        </h:outputText>
                    </p:column>
                    
                    <p:column headerText="Valor Total" width="120" style="text-align:right;">
                        <h:outputText value="#{pedido.valorTotal}">
                            <f:convertNumber type="currency" currencySymbol="R$ " />
                        </h:outputText>
                    </p:column>
                    
                    <p:column headerText="Ação" width="150" style="text-align:center;">
                        <p:commandButton value="Marcar como Pago (Alt+P)" icon="pi pi-dollar" 
                                         actionListener="#{vendaPedidoController.prepararMarcarComoPago(pedido)}"
                                         onclick="PF('dlgMarcarPago').show(); return false;"
                                         styleClass="action-button-success" accesskey="P" />
                    </p:column>
                </p:dataTable>
            </p:fieldset>
            
            <!-- Dialog: Marcar como Pago -->
            <p:dialog id="dlgMarcarPago" header="Marcar como Pago" widgetVar="dlgMarcarPago" 
                      modal="true" responsive="true" width="450" closeOnEscape="true">
                
                <div class="dialog-content">
                    <p:panelGrid columns="2" layout="grid" styleClass="modern-panel-grid">
                        <p:outputLabel value="Pedido:" styleClass="label-bold" />
                        <p:outputLabel value="#{vendaPedidoController.pedidoParaPagamento.numeroPedido}" />
                        
                        <p:outputLabel value="Trabalhador:" styleClass="label-bold" />
                        <p:outputLabel value="#{vendaPedidoController.pedidoParaPagamento.trabalhadorResponsavel.pessoa.nome}" />
                        
                        <p:outputLabel value="Valor Total:" styleClass="label-bold" />
                        <h:outputText value="#{vendaPedidoController.pedidoParaPagamento.valorTotal}">
                            <f:convertNumber type="currency" currencySymbol="R$ " />
                        </h:outputText>
                    </p:panelGrid>
                    
                    <p:separator />
                    
                    <div class="field-grid">
                        <div class="field-group">
                            <label for="formaPagamentoPago">Forma de Pagamento: *</label>
                            <p:selectOneMenu id="formaPagamentoPago" value="#{vendaPedidoController.formaPagamento}" required="true">
                                <f:selectItem itemLabel="-- Selecione --" itemValue="#{null}" />
                                <f:selectItem itemLabel="Dinheiro" itemValue="DINHEIRO" />
                                <f:selectItem itemLabel="PIX" itemValue="PIX" />
                                <f:selectItem itemLabel="Cartão de Débito" itemValue="DEBITO" />
                                <f:selectItem itemLabel="Cartão de Crédito" itemValue="CREDITO" />
                            </p:selectOneMenu>
                        </div>
                        
                        <div class="field-group">
                            <label for="valorPagamento">Valor Pago: *</label>
                            <p:inputNumber id="valorPagamento" value="#{vendaPedidoController.valorPagamento}" 
                                           mode="currency" currencySymbol="R$ " required="true" />
                        </div>
                    </div>
                </div>
                
                <f:facet name="footer">
                    <p:commandButton value="Confirmar Pagamento" icon="pi pi-check" 
                                     actionListener="#{vendaPedidoController.marcarComoPago}"
                                     update="@form" oncomplete="PF('dlgMarcarPago').hide();"
                                     styleClass="action-button-primary" />
                    
                    <p:commandButton value="Cancelar" icon="pi pi-times" 
                                     onclick="PF('dlgMarcarPago').hide(); return false;"
                                     styleClass="action-button-secondary" />
                </f:facet>
            </p:dialog>
        </h:form>
    </ui:define>
</ui:composition>
</html>
```

---

## 2. PÁGINA: Gerenciamento de Artigos (Lista)

**Arquivo**: `pages/venda/artigos.xhtml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:h="http://xmlns.jcp.org/jsf/html"
      xmlns:f="http://xmlns.jcp.org/jsf/core"
      xmlns:ui="http://xmlns.jcp.org/jsf/facelets"
      xmlns:p="http://primefaces.org/ui">

<ui:composition template="/templates/template.xhtml">
    <ui:define name="title">Gerenciar Artigos</ui:define>
    
    <ui:define name="content">
        <h:form id="formArtigos" styleClass="modern-form">
            
            <p:growl id="growl" showDetail="true" sticky="false" life="5000">
                <p:autoUpdate />
            </p:growl>
            
            <!-- Título e Barra de Ações -->
            <div class="page-header">
                <h2>Gerenciamento de Artigos</h2>
                <div class="action-buttons">
                    <p:commandButton value="Novo Artigo (Alt+N)" icon="pi pi-plus" 
                                     action="#{vendaArtigoController.prepararNovo}"
                                     onclick="PF('dlgArtigo').show(); return false;"
                                     styleClass="action-button-primary" accesskey="N" />
                    
                    <p:commandButton value="Atualizar Lista (Alt+R)" icon="pi pi-refresh" 
                                     actionListener="#{vendaArtigoController.carregarArtigos}"
                                     update="tblArtigos" styleClass="action-button-secondary" accesskey="R" />
                </div>
            </div>
            
            <!-- Filters -->
            <p:fieldset legend="Filtros" toggleable="true" collapsed="false" styleClass="modern-fieldset">
                <div class="field-grid">
                    <div class="field-group">
                        <label for="filtroNome">Nome:</label>
                        <p:inputText id="filtroNome" value="#{vendaArtigoController.filtroNome}" 
                                     placeholder="Digite o nome do artigo" />
                    </div>
                    
                    <div class="field-group">
                        <label for="filtroCategoria">Categoria:</label>
                        <p:selectOneMenu id="filtroCategoria" value="#{vendaArtigoController.filtroCategoria}">
                            <f:selectItem itemLabel="Todas" itemValue="#{null}" />
                            <f:selectItems value="#{vendaArtigoController.categorias}" />
                        </p:selectOneMenu>
                    </div>
                    
                    <div class="field-group">
                        <label for="filtroAtivo">Status:</label>
                        <p:selectOneMenu id="filtroAtivo" value="#{vendaArtigoController.filtroAtivo}">
                            <f:selectItem itemLabel="Todos" itemValue="#{null}" />
                            <f:selectItem itemLabel="Ativos" itemValue="true" />
                            <f:selectItem itemLabel="Inativos" itemValue="false" />
                        </p:selectOneMenu>
                    </div>
                </div>
                
                <div class="form-buttons">
                    <p:commandButton value="Filtrar (Alt+F)" icon="pi pi-filter" 
                                     actionListener="#{vendaArtigoController.filtrarArtigos}"
                                     update="tblArtigos" styleClass="action-button-primary" accesskey="F" />
                    
                    <p:commandButton value="Limpar Filtros" icon="pi pi-filter-slash" 
                                     actionListener="#{vendaArtigoController.limparFiltros}"
                                     update="@form" styleClass="action-button-secondary" />
                </div>
            </p:fieldset>
            
            <!-- Tabela de Artigos -->
            <p:fieldset legend="Artigos Cadastrados" toggleable="false" styleClass="modern-fieldset">
                <p:dataTable id="tblArtigos" value="#{vendaArtigoController.artigos}" 
                             var="art" emptyMessage="Nenhum artigo encontrado"
                             paginator="true" rows="15" paginatorPosition="bottom"
                             styleClass="modern-datatable" reflow="true">
                    
                    <p:column headerText="Código" width="100" sortBy="#{art.codigo}">
                        <h:outputText value="#{art.codigo}" />
                    </p:column>
                    
                    <p:column headerText="Nome" sortBy="#{art.nome}">
                        <h:outputText value="#{art.nome}" />
                    </p:column>
                    
                    <p:column headerText="Categoria" width="150" sortBy="#{art.categoria}">
                        <h:outputText value="#{art.categoria}" />
                    </p:column>
                    
                    <p:column headerText="Preço Base" width="120" style="text-align:right;">
                        <h:outputText value="#{art.precoBase}">
                            <f:convertNumber type="currency" currencySymbol="R$ " />
                        </h:outputText>
                    </p:column>
                    
                    <p:column headerText="Estoque" width="100" style="text-align:center;">
                        <h:outputText value="#{art.estoqueAtual}"
                                      styleClass="#{art.estoqueAtual le art.estoqueMinimo ? 'estoque-baixo' : ''}" />
                    </p:column>
                    
                    <p:column headerText="Status" width="100" style="text-align:center;">
                        <p:tag value="#{art.ativo ? 'ATIVO' : 'INATIVO'}" 
                               severity="#{art.ativo ? 'success' : 'danger'}" />
                    </p:column>
                    
                    <p:column headerText="Ações" width="200" style="text-align:center;">
                        <p:commandButton icon="pi pi-pencil" title="Editar" 
                                         actionListener="#{vendaArtigoController.prepararEdicao(art)}"
                                         onclick="PF('dlgArtigo').show(); return false;"
                                         update="dlgArtigoContent" styleClass="action-button-edit" />
                        
                        <p:commandButton icon="#{art.ativo ? 'pi pi-eye-slash' : 'pi pi-eye'}" 
                                         title="#{art.ativo ? 'Inativar' : 'Ativar'}"
                                         actionListener="#{art.ativo ? vendaArtigoController.inativar(art) : vendaArtigoController.ativar(art)}"
                                         update="@form" styleClass="action-button-toggle">
                            <p:confirm header="Confirmação" 
                                       message="Deseja realmente #{art.ativo ? 'inativar' : 'ativar'} este artigo?" 
                                       icon="pi pi-exclamation-triangle" />
                        </p:commandButton>
                        
                        <p:commandButton icon="pi pi-trash" title="Excluir" 
                                         actionListener="#{vendaArtigoController.excluir(art)}"
                                         update="@form" styleClass="action-button-delete">
                            <p:confirm header="Confirmação" 
                                       message="Deseja realmente excluir este artigo? Esta ação não pode ser desfeita." 
                                       icon="pi pi-exclamation-triangle" />
                        </p:commandButton>
                    </p:column>
                </p:dataTable>
                
                <p:confirmDialog global="true" showEffect="fade" hideEffect="fade" responsive="true" width="350">
                    <p:commandButton value="Não" type="button" styleClass="ui-confirmdialog-no" icon="pi pi-times" />
                    <p:commandButton value="Sim" type="button" styleClass="ui-confirmdialog-yes" icon="pi pi-check" />
                </p:confirmDialog>
            </p:fieldset>
            
            <!-- Dialog: Cadastro/Edição de Artigo -->
            <p:dialog id="dlgArtigo" header="#{vendaArtigoController.artigo.id == null ? 'Novo Artigo' : 'Editar Artigo'}" 
                      widgetVar="dlgArtigo" modal="true" responsive="true" width="650" closeOnEscape="true">
                
                <h:panelGroup id="dlgArtigoContent">
                    <div class="dialog-content">
                        <p:fieldset legend="Informações Básicas" styleClass="modern-fieldset">
                            <div class="field-grid">
                                <div class="field-group">
                                    <label for="codigo">Código:</label>
                                    <p:inputText id="codigo" value="#{vendaArtigoController.artigo.codigo}" 
                                                 placeholder="Ex: ART001" maxlength="50" />
                                </div>
                                
                                <div class="field-group">
                                    <label for="nome">Nome: *</label>
                                    <p:inputText id="nome" value="#{vendaArtigoController.artigo.nome}" 
                                                 placeholder="Nome do artigo" maxlength="100" required="true" />
                                </div>
                                
                                <div class="field-group">
                                    <label for="categoria">Categoria:</label>
                                    <p:inputText id="categoria" value="#{vendaArtigoController.artigo.categoria}" 
                                                 placeholder="Ex: Camisetas" maxlength="50" />
                                </div>
                                
                                <div class="field-group">
                                    <label for="precoBase">Preço Base: *</label>
                                    <p:inputNumber id="precoBase" value="#{vendaArtigoController.artigo.precoBase}" 
                                                   mode="currency" currencySymbol="R$ " minValue="0" required="true" />
                                </div>
                            </div>
                        </p:fieldset>
                        
                        <p:fieldset legend="Estoque" styleClass="modern-fieldset">
                            <div class="field-grid">
                                <div class="field-group">
                                    <label for="estoqueAtual">Estoque Atual:</label>
                                    <p:inputNumber id="estoqueAtual" value="#{vendaArtigoController.artigo.estoqueAtual}" 
                                                   decimalPlaces="0" minValue="0" />
                                </div>
                                
                                <div class="field-group">
                                    <label for="estoqueMinimo">Estoque Mínimo:</label>
                                    <p:inputNumber id="estoqueMinimo" value="#{vendaArtigoController.artigo.estoqueMinimo}" 
                                                   decimalPlaces="0" minValue="0" />
                                </div>
                            </div>
                        </p:fieldset>
                        
                        <p:fieldset legend="Informações Adicionais" styleClass="modern-fieldset">
                            <div class="field-grid">
                                <div class="field-group">
                                    <label for="descricao">Descrição:</label>
                                    <p:inputTextarea id="descricao" value="#{vendaArtigoController.artigo.descricao}" 
                                                     rows="3" maxlength="500" placeholder="Descrição detalhada do artigo" />
                                </div>
                                
                                <div class="field-group">
                                    <label for="fotoUrl">URL da Foto:</label>
                                    <p:inputText id="fotoUrl" value="#{vendaArtigoController.artigo.fotoUrl}" 
                                                 placeholder="https://" maxlength="255" />
                                </div>
                            </div>
                        </p:fieldset>
                    </div>
                </h:panelGroup>
                
                <f:facet name="footer">
                    <p:commandButton value="Salvar (Alt+S)" icon="pi pi-check" 
                                     actionListener="#{vendaArtigoController.salvar}"
                                     update="@form" oncomplete="if (args.validationFailed) return; PF('dlgArtigo').hide();"
                                     styleClass="action-button-primary" accesskey="S" />
                    
                    <p:commandButton value="Cancelar" icon="pi pi-times" 
                                     onclick="PF('dlgArtigo').hide(); return false;"
                                     styleClass="action-button-secondary" />
                </f:facet>
            </p:dialog>
        </h:form>
    </ui:define>
</ui:composition>
</html>
```

---

## 3. PÁGINA: Consulta de Vendas e Relatórios

**Arquivo**: `pages/venda/consulta-vendas.xhtml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:h="http://xmlns.jcp.org/jsf/html"
      xmlns:f="http://xmlns.jcp.org/jsf/core"
      xmlns:ui="http://xmlns.jcp.org/jsf/facelets"
      xmlns:p="http://primefaces.org/ui">

<ui:composition template="/templates/template.xhtml">
    <ui:define name="title">Consulta de Vendas</ui:define>
    
    <ui:define name="content">
        <h:form id="formConsultaVendas" styleClass="modern-form">
            
            <p:growl id="growl" showDetail="true" sticky="false" life="5000">
                <p:autoUpdate />
            </p:growl>
            
            <div class="page-header">
                <h2>Consulta de Vendas e Relatórios</h2>
            </div>
            
            <!-- Filtros de Consulta -->
            <p:fieldset legend="Filtros de Consulta" toggleable="true" collapsed="false" styleClass="modern-fieldset">
                <div class="field-grid">
                    <div class="field-group">
                        <label for="filtroEncontroConsulta">Encontro:</label>
                        <p:selectOneMenu id="filtroEncontroConsulta" value="#{vendaPedidoController.filtroEncontroId}">
                            <f:selectItem itemLabel="Todos" itemValue="#{null}" />
                            <f:selectItems value="#{vendaPedidoController.todosEncontros}" 
                                           var="enc" itemLabel="#{enc.nome}" itemValue="#{enc.id}"/>
                        </p:selectOneMenu>
                    </div>
                    
                    <div class="field-group">
                        <label for="filtroStatus">Status:</label>
                        <p:selectOneMenu id="filtroStatus" value="#{vendaPedidoController.filtroStatus}">
                            <f:selectItem itemLabel="Todos" itemValue="#{null}" />
                            <f:selectItems value="#{vendaPedidoController.todosStatus}" 
                                           var="st" itemLabel="#{st.descricao}" itemValue="#{st}"/>
                        </p:selectOneMenu>
                    </div>
                    
                    <div class="field-group">
                        <label for="dataInicio">Data Início:</label>
                        <p:calendar id="dataInicio" value="#{vendaPedidoController.dataInicio}" 
                                    pattern="dd/MM/yyyy" showButtonBar="true" />
                    </div>
                    
                    <div class="field-group">
                        <label for="dataFim">Data Fim:</label>
                        <p:calendar id="dataFim" value="#{vendaPedidoController.dataFim}" 
                                    pattern="dd/MM/yyyy" showButtonBar="true" />
                    </div>
                </div>
                
                <div class="form-buttons">
                    <p:commandButton value="Consultar (Alt+C)" icon="pi pi-search" 
                                     actionListener="#{vendaPedidoController.filtrarPedidos}"
                                     update="tblPedidos pnlTotalizadores" styleClass="action-button-primary" accesskey="C" />
                    
                    <p:commandButton value="Limpar" icon="pi pi-filter-slash" 
                                     actionListener="#{vendaPedidoController.limparFiltros}"
                                     update="@form" styleClass="action-button-secondary" />
                    
                    <p:commandButton value="Exportar Excel (Alt+E)" icon="pi pi-file-excel" 
                                     ajax="false" styleClass="action-button-success" accesskey="E">
                        <p:dataExporter type="xlsx" target="tblPedidos" fileName="vendas_#{vendaPedidoController.dataExportacao}" />
                    </p:commandButton>
                </div>
            </p:fieldset>
            
            <!-- Totalizadores -->
            <p:fieldset id="pnlTotalizadores" legend="Totalizadores" toggleable="true" collapsed="false" styleClass="modern-fieldset">
                <p:panelGrid columns="4" layout="grid" styleClass="dashboard-grid">
                    <div class="dashboard-card card-info">
                        <div class="card-icon"><i class="pi pi-shopping-cart"></i></div>
                        <div class="card-content">
                            <span class="card-label">Total de Pedidos</span>
                            <span class="card-value">#{vendaPedidoController.totalPedidos}</span>
                        </div>
                    </div>
                    
                    <div class="dashboard-card card-success">
                        <div class="card-icon"><i class="pi pi-check-circle"></i></div>
                        <div class="card-content">
                            <span class="card-label">Pedidos Pagos</span>
                            <span class="card-value">
                                <h:outputText value="#{vendaPedidoController.totalValorPago}">
                                    <f:convertNumber type="currency" currencySymbol="R$ " />
                                </h:outputText>
                            </span>
                        </div>
                    </div>
                    
                    <div class="dashboard-card card-warning">
                        <div class="card-icon"><i class="pi pi-clock"></i></div>
                        <div class="card-content">
                            <span class="card-label">Aguardando Pagamento</span>
                            <span class="card-value">
                                <h:outputText value="#{vendaPedidoController.totalAguardandoPagamento}">
                                    <f:convertNumber type="currency" currencySymbol="R$ " />
                                </h:outputText>
                            </span>
                        </div>
                    </div>
                    
                    <div class="dashboard-card card-primary">
                        <div class="card-icon"><i class="pi pi-dollar"></i></div>
                        <div class="card-content">
                            <span class="card-label">Valor Total</span>
                            <span class="card-value">
                                <h:outputText value="#{vendaPedidoController.valorTotalGeral}">
                                    <f:convertNumber type="currency" currencySymbol="R$ " />
                                </h:outputText>
                            </span>
                        </div>
                    </div>
                </p:panelGrid>
            </p:fieldset>
            
            <!-- Tabela de Pedidos -->
            <p:fieldset legend="Pedidos" toggleable="true" collapsed="false" styleClass="modern-fieldset">
                <p:dataTable id="tblPedidos" value="#{vendaPedidoController.pedidosFiltrados}" 
                             var="pedido" emptyMessage="Nenhum pedido encontrado"
                             paginator="true" rows="20" paginatorPosition="bottom"
                             styleClass="modern-datatable" reflow="true">
                    
                    <p:column headerText="Número" width="120" sortBy="#{pedido.numeroPedido}">
                        <h:outputText value="#{pedido.numeroPedido}" />
                    </p:column>
                    
                    <p:column headerText="Encontro" sortBy="#{pedido.encontro.nome}">
                        <h:outputText value="#{pedido.encontro.nome}" />
                    </p:column>
                    
                    <p:column headerText="Trabalhador" sortBy="#{pedido.trabalhadorResponsavel.pessoa.nome}">
                        <h:outputText value="#{pedido.trabalhadorResponsavel.pessoa.nome}" />
                    </p:column>
                    
                    <p:column headerText="Data Abertura" width="140" sortBy="#{pedido.dataAbertura}">
                        <h:outputText value="#{pedido.dataAbertura}">
                            <f:convertDateTime pattern="dd/MM/yyyy HH:mm" />
                        </h:outputText>
                    </p:column>
                    
                    <p:column headerText="Data Fechamento" width="140" sortBy="#{pedido.dataFechamento}">
                        <h:outputText value="#{pedido.dataFechamento}">
                            <f:convertDateTime pattern="dd/MM/yyyy HH:mm" />
                        </h:outputText>
                    </p:column>
                    
                    <p:column headerText="Qtd. Itens" width="80" style="text-align:center;">
                        <h:outputText value="#{pedido.quantidadeTotalItens}" />
                    </p:column>
                    
                    <p:column headerText="Valor Total" width="120" style="text-align:right;" sortBy="#{pedido.valorTotal}">
                        <h:outputText value="#{pedido.valorTotal}">
                            <f:convertNumber type="currency" currencySymbol="R$ " />
                        </h:outputText>
                    </p:column>
                    
                    <p:column headerText="Status" width="120" sortBy="#{pedido.status}">
                        <p:tag value="#{pedido.status.descricao}" 
                               severity="#{pedido.status == 'PAGO' ? 'success' : pedido.status == 'AGUARDO_PAGAMENTO' ? 'warning' : pedido.status == 'CANCELADO' ? 'danger' : 'info'}" />
                    </p:column>
                    
                    <p:column headerText="Forma Pag." width="120">
                        <h:outputText value="#{pedido.formaPagamento}" />
                    </p:column>
                    
                    <p:column headerText="Ações" width="100" style="text-align:center;">
                        <p:commandButton icon="pi pi-eye" title="Ver Detalhes" 
                                         actionListener="#{vendaPedidoController.visualizarDetalhes(pedido)}"
                                         onclick="PF('dlgDetalhes').show(); return false;"
                                         update="dlgDetalhesContent" styleClass="action-button-info" />
                    </p:column>
                </p:dataTable>
            </p:fieldset>
            
            <!-- Dialog: Detalhes do Pedido -->
            <p:dialog id="dlgDetalhes" header="Detalhes do Pedido #{vendaPedidoController.pedidoSelecionado.numeroPedido}" 
                      widgetVar="dlgDetalhes" modal="true" responsive="true" width="800" closeOnEscape="true">
                
                <h:panelGroup id="dlgDetalhesContent">
                    <p:fieldset legend="Informações do Pedido" styleClass="modern-fieldset">
                        <p:panelGrid columns="2" layout="grid" styleClass="modern-panel-grid">
                            <p:outputLabel value="Número:" styleClass="label-bold" />
                            <p:outputLabel value="#{vendaPedidoController.pedidoSelecionado.numeroPedido}" />
                            
                            <p:outputLabel value="Encontro:" styleClass="label-bold" />
                            <p:outputLabel value="#{vendaPedidoController.pedidoSelecionado.encontro.nome}" />
                            
                            <p:outputLabel value="Trabalhador Responsável:" styleClass="label-bold" />
                            <p:outputLabel value="#{vendaPedidoController.pedidoSelecionado.trabalhadorResponsavel.pessoa.nome}" />
                            
                            <p:outputLabel value="Data Abertura:" styleClass="label-bold" />
                            <h:outputText value="#{vendaPedidoController.pedidoSelecionado.dataAbertura}">
                                <f:convertDateTime pattern="dd/MM/yyyy HH:mm" />
                            </h:outputText>
                            
                            <p:outputLabel value="Data Fechamento:" styleClass="label-bold" />
                            <h:outputText value="#{vendaPedidoController.pedidoSelecionado.dataFechamento}">
                                <f:convertDateTime pattern="dd/MM/yyyy HH:mm" />
                            </h:outputText>
                            
                            <p:outputLabel value="Status:" styleClass="label-bold" />
                            <p:tag value="#{vendaPedidoController.pedidoSelecionado.status.descricao}" 
                                   severity="#{vendaPedidoController.pedidoSelecionado.status == 'PAGO' ? 'success' : 'warning'}" />
                            
                            <p:outputLabel value="Forma de Pagamento:" styleClass="label-bold" />
                            <p:outputLabel value="#{vendaPedidoController.pedidoSelecionado.formaPagamento}" />
                            
                            <p:outputLabel value="Valor Total:" styleClass="label-bold" />
                            <h:outputText value="#{vendaPedidoController.pedidoSelecionado.valorTotal}">
                                <f:convertNumber type="currency" currencySymbol="R$ " />
                            </h:outputText>
                            
                            <p:outputLabel value="Valor Pago:" styleClass="label-bold" />
                            <h:outputText value="#{vendaPedidoController.pedidoSelecionado.valorPago}">
                                <f:convertNumber type="currency" currencySymbol="R$ " />
                            </h:outputText>
                        </p:panelGrid>
                    </p:fieldset>
                    
                    <p:fieldset legend="Itens do Pedido" styleClass="modern-fieldset">
                        <p:dataTable value="#{vendaPedidoController.pedidoSelecionado.itens}" 
                                     var="item" emptyMessage="Sem itens"
                                     styleClass="modern-datatable">
                            
                            <p:column headerText="Artigo">
                                <h:outputText value="#{item.artigo.nome}" />
                            </p:column>
                            
                            <p:column headerText="Quantidade" width="100" style="text-align:center;">
                                <h:outputText value="#{item.quantidade}" />
                            </p:column>
                            
                            <p:column headerText="Valor Unitário" width="120" style="text-align:right;">
                                <h:outputText value="#{item.valorUnitario}">
                                    <f:convertNumber type="currency" currencySymbol="R$ " />
                                </h:outputText>
                            </p:column>
                            
                            <p:column headerText="Valor Total" width="120" style="text-align:right;">
                                <h:outputText value="#{item.valorTotalItem}">
                                    <f:convertNumber type="currency" currencySymbol="R$ " />
                                </h:outputText>
                            </p:column>
                        </p:dataTable>
                    </p:fieldset>
                </h:panelGroup>
                
                <f:facet name="footer">
                    <p:commandButton value="Fechar" icon="pi pi-times" 
                                     onclick="PF('dlgDetalhes').hide(); return false;"
                                     styleClass="action-button-secondary" />
                </f:facet>
            </p:dialog>
        </h:form>
    </ui:define>
</ui:composition>
</html>
```

---

## 4. RESUMO DOS ATALHOS DE TECLADO (accesskey)

### Página: gestao-vendas.xhtml
- **Alt+N** → Iniciar Nova Venda (botão "Nova Venda")
- **Alt+I** → Adicionar Item ao pedido aberto
- **Alt+F** → Abrir dialog para Fechar Pedido (só com itens)
- **Alt+P** → Marcar pedido como Pago (na lista "Aguardando Pagamento")

### Página: artigos.xhtml
- **Alt+N** → Abrir dialog para Novo Artigo
- **Alt+R** → Atualizar/Refresh lista de artigos
- **Alt+F** → Aplicar filtros
- **Alt+S** → Salvar artigo (dentro do dialog)

### Página: consulta-vendas.xhtml
- **Alt+C** → Executar consulta com filtros
- **Alt+E** → Exportar para Excel

---

## 5. CSS ADICIONAL SUGERIDO

Para melhorar a visualização, adicionar em `resources/css/style.css` (ou equivalente):

```css
/* Dashboard Cards */
.dashboard-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 1rem;
    margin-bottom: 1.5rem;
}

.dashboard-card {
    display: flex;
    align-items: center;
    padding: 1.5rem;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    transition: transform 0.2s;
}

.dashboard-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.dashboard-card .card-icon {
    font-size: 2.5rem;
    margin-right: 1rem;
    opacity: 0.8;
}

.dashboard-card .card-content {
    display: flex;
    flex-direction: column;
}

.dashboard-card .card-label {
    font-size: 0.875rem;
    color: rgba(0,0,0,0.6);
    margin-bottom: 0.25rem;
}

.dashboard-card .card-value {
    font-size: 1.5rem;
    font-weight: bold;
    color: rgba(0,0,0,0.9);
}

.card-info { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; }
.card-success { background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); color: white; }
.card-warning { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); color: white; }
.card-primary { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); color: white; }

.card-info .card-label,
.card-success .card-label,
.card-warning .card-label,
.card-primary .card-label {
    color: rgba(255,255,255,0.9);
}

/* Action Buttons */
.action-button-primary {
    background-color: #007bff;
    border-color: #007bff;
    color: white;
}

.action-button-success {
    background-color: #28a745;
    border-color: #28a745;
    color: white;
}

.action-button-edit {
    background-color: #ffc107;
    border-color: #ffc107;
    color: #212529;
}

.action-button-delete {
    background-color: #dc3545;
    border-color: #dc3545;
    color: white;
}

.action-button-toggle {
    background-color: #6c757d;
    border-color: #6c757d;
    color: white;
}

.action-button-info {
    background-color: #17a2b8;
    border-color: #17a2b8;
    color: white;
}

/* Modern Form Elements */
.modern-form {
    max-width: 100%;
    margin: 0 auto;
}

.modern-fieldset {
    margin-bottom: 1.5rem;
    border: 1px solid #ddd;
    border-radius: 8px;
    padding: 1rem;
}

.modern-datatable {
    border-radius: 8px;
    overflow: hidden;
}

.field-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 1rem;
    margin-bottom: 1rem;
}

.field-group {
    display: flex;
    flex-direction: column;
}

.field-group label {
    font-weight: 600;
    margin-bottom: 0.5rem;
    color: #333;
}

.form-buttons {
    display: flex;
    gap: 0.5rem;
    margin-top: 1rem;
    flex-wrap: wrap;
}

.page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1.5rem;
}

.page-header h2 {
    margin: 0;
    color: #333;
}

.action-buttons {
    display: flex;
    gap: 0.5rem;
}

/* Status Indicators */
.status-active {
    color: #28a745;
    font-weight: bold;
}

.status-inactive {
    color: #dc3545;
    font-weight: bold;
}

.estoque-baixo {
    color: #dc3545;
    font-weight: bold;
}

/* Dialog Customization */
.dialog-content {
    padding: 1rem 0;
}

.info-panel {
    background-color: #e7f3ff;
    border: 1px solid #b3d7ff;
    border-radius: 6px;
    padding: 1rem;
    margin-bottom: 1rem;
}

.modern-panel-grid {
    width: 100%;
}

.label-bold {
    font-weight: 600;
    color: #555;
}
```

---

## 6. INTEGRAÇÃO NO MENU

Editar o arquivo `templates/menu.xhtml` e adicionar a seção de Vendas:

```xml
<!-- Seção: Vendas -->
<li>
    <div class="menu-section">
        <span class="submenu-label">
            <i class="pi pi-shopping-cart section-icon"></i>
            Vendas
        </span>
        <ul class="submenu">
            <li>
                <h:link outcome="/pages/venda/gestao-vendas" styleClass="menu-link">
                    <i class="pi pi-shopping-bag"></i>
                    <span>Gestão de Vendas</span>
                </h:link>
            </li>
            <li>
                <h:link outcome="/pages/venda/artigos" styleClass="menu-link">
                    <i class="pi pi-box"></i>
                    <span>Artigos</span>
                </h:link>
            </li>
            <li>
                <h:link outcome="/pages/venda/consulta-vendas" styleClass="menu-link">
                    <i class="pi pi-chart-line"></i>
                    <span>Consultar Vendas</span>
                </h:link>
            </li>
        </ul>
    </div>
</li>
```

---

## 7. NOTAS IMPORTANTES

### 7.1 Navegação com Teclado
- Os atributos `accesskey` funcionam combinando **Alt + letra** no Windows/Linux
- No macOS: **Control + Alt + letra**
- Apenas letras únicas por página para evitar conflitos

### 7.2 Foco Automático
Para melhorar UX, adicionar JavaScript para focar no primeiro campo ao abrir dialogs:

```javascript
PF('dlgArtigo').show();
setTimeout(function() {
    document.getElementById('formArtigos:nome_input').focus();
}, 100);
```

### 7.3 Validação Client-Side
PrimeFaces oferece validação automática com `required="true"` e bean validation annotations. Adicionar JavaScript customizado se precisar de validações mais complexas.

### 7.4 Performance
Para listas grandes (>500 itens), considerar:
- Lazy loading em `<p:dataTable lazy="true">`
- Aumentar `rows` do paginator ou implementar busca server-side
- Cache de listas estáticas (categorias, status)

---

**FIM DO DOCUMENTO DE PÁGINAS XHTML**
