> **Prévia do redesign 0.4:** estas capturas e binários pertencem ao código testado na branch [ui-overhaul-20261022](https://github.com/AttackAlien/Alien-Client/tree/ui-overhaul-20261022). A integração com o Unified/Sky Studio Ultimate mais recente está no PR do redesign; o código atual da main foi preservado.

# Interface 0.4.0-alpha.1

O redesign preserva os motores e a separação Legit/Full. A interface usa um sistema compartilhado de componentes desenhados pelo Alien; GuiButton/GuiScreen ficam como adaptadores de entrada do Forge, sem usar o desenho dos botões vanilla.

## O que mudou

- Open Sans Regular/Semibold em dois atlas antialias de resolução 4×, com métricas próprias e suporte aos textos latinos da interface. É a fonte padrão; Minecraft permanece uma opção de compatibilidade.
- Painéis arredondados com bordas suavizadas, gradientes, sombras, transparência, ícones vetoriais originais e estados de hover/pressão.
- Botões, switches, sliders com entrada numérica, campos próprios, seletor HSV/RGBA/HEX, componente dropdown, tooltips, notificações e modais de confirmação reutilizáveis.
- ClickGUI com sidebar, pesquisa instantânea, filtros de ativos/favoritos, cartões reais do registro e editor de atalhos por módulo. As categorias avançadas Full ainda não possuem módulos implementados; não foram criados botões fictícios para eles.
- Cosmetic Studio com navegação lateral, área 3D maior, miniaturas das malhas reais, iluminação/fundo configuráveis e câmera com rotação/zoom interpolados. Jogadores por UUID, transformações, animações, cores e importações foram preservados.
- World Studio com galeria das seis paletas, prévia do gradiente/panorama, intensidade do céu, cores, visibilidade de nuvens, tamanho/visibilidade de sol/lua, precipitação, neblina e importador PNG. A prévia indicada como Palette preview é um controle da interface, não uma fotografia ou promessa de novo skybox cinematográfico.
- HUD Studio com seleção, arraste, alça de escala, alinhamento, snap, grade pontilhada, fonte/fundo/gradiente/sombra/opacidade e import/export somente do HUD.
- Theme Studio com Alien Purple, Midnight Black, Cyber Cyan, Crimson Red, Minimal White e Custom RGB; tokens de cores, raio, transparência, sombra, tipografia, movimento, hover e escala persistentes.
- Desfoque opcional real: dois passes Gaussianos GLSL 1.20 em framebuffers a um quarto da resolução, aplicados sobre o mundo. Começa desligado; depende de suporte a shaders/framebuffer.

## Usar

Right Shift abre o ClickGUI. Clique no cartão para abrir detalhes e capturar uma tecla; Delete/Backspace remove o atalho. A estrela marca favoritos. Settings abre preferências/perfis e Themes abre o Theme Studio.

No Theme Studio, os canais podem ser RGBA ou HSV; Enter aplica os campos ou HEX. Os sliders apresentam edição numérica pelo valor. A escala do menu altera desenho e coordenadas de entrada; em janelas pequenas o zoom é limitado para preservar um canvas lógico mínimo de 320×240. Reduzir movimento ou desligar animações elimina interpolações opcionais.

No HUD Studio, selecione na lista e arraste até o canvas; use a alça inferior direita ou a roda para escala. Shift ignora o snap. Export grava `hud/exports/layout.json`; Import lê `hud/imports/layout.json` na raiz de configuração da respectiva edição. Esse formato não substitui as demais configurações do cliente e limita o arquivo a 64 KiB/profundidade 16.

## Persistência e limites

`config.ui` guarda os tokens de tema; `interfacePrefs` guarda categoria/filtros/favoritos; `moduleKeys` mantém os atalhos. Perfis e temas continuam compatíveis com os formatos anteriores. O tema avançado é incluído nos arquivos de ThemeLibrary; modelos/skyboxes permanecem em suas pastas locais.

O redesign não conclui o master plan de módulos avançados. Fontes arbitrárias enviadas pelo usuário, tipografia CJK completa, outros formatos de modelos/cubemaps/OptiFine Custom Sky, texturas arbitrárias de sol/lua e física de colisão da capa continuam fora da implementação atual. Algumas opções discretas usam botões de ciclo; o componente dropdown está disponível na biblioteca. As animações usam fade-in, hover, câmera e rolagem; não há uma transição de saída entre todas as telas.

Os itens e o mundo mantêm a geometria/texturas do Minecraft. O desfoque opcional e as sombras têm custo gráfico; a execução em software não estabelece ganho de FPS. Consulte a [validação](UI-OVERHAUL-VALIDATION.md) para resultados e limites efetivos.
