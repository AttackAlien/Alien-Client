> A Legit usa `config/alien-client/`; a Full usa `config/alien-client-full/`. Os subdiretórios e limites abaixo valem para ambas as composições. A Full da fase 1 ainda só possui recursos visuais compartilhados.

# Recursos e perfis locais

Diretório base: `<pasta do jogo>/config/alien-client/`. Crie os subdiretórios ao copiar os arquivos, se necessário. Recursos não são baixados automaticamente.

## Modelos externos e acessórios integrados

Os chapéus e acessórios integrados da versão 0.2.0 usam malhas procedurais próprias, incluindo superfícies curvas. Esse renderizador não amplia o formato de importação: arquivos externos continuam sendo modelos de caixas JSON limitados. Não há importador OBJ, glTF, Blockbench ou de animações externas.

Coloque `meu-chapeu.json` em `cosmetics/`:

```json
{
  "boxes": [
    {"x": -2, "y": -11, "z": -2, "width": 4, "height": 3, "depth": 4}
  ]
}
```

As coordenadas são pixels do modelo Minecraft, com a cabeça como ponto de referência. Use de 1 a 128 caixas, posições entre -64 e 64 e dimensões inteiras entre 1 e 32. Limite de 1 MiB por arquivo. Na aba **Imports** do Cosmetic Studio, **Create model example** cria um exemplo sem sobrescrever arquivos existentes. **Reload local assets** limpa o cache; selecione o arquivo com o botão Model.

A textura é opcional: PNG real em `cosmetics/`, de até 1024 × 1024 e 1 MiB. O layout UV do modelo usa uma base 64 × 64 compartilhada pelas caixas. Selecione-a com o botão Texture. A importação é para esse formato de caixas, não para formatos externos de modelagem.

Os recursos ficam em cache por nome, com no máximo 32 combinações de modelo/textura. Após editar um arquivo no disco, use Reload. Modelos inválidos são ignorados, com mensagem na aba Imports.

Na aba **Motion**, a capa integrada oferece emblemas **Alien**, **Moon**, **Stars** e **Plain**, além de cor principal e de acabamento. Essas texturas são geradas localmente; seus UVs acompanham o movimento da malha. O campo Texture da aba Imports se aplica ao modelo externo preso à cabeça, não à capa integrada. Não há importador de arte arbitrária para os emblemas da capa.

## Céu 360°: panoramas e cubemaps

O World Studio continua aceitando **panoramas equiretangulares PNG** em `skyboxes/`. Informe um nome como `galaxia.png`, ative **Custom sky** e use **Apply RGB / PNG**. As mudanças no arquivo são detectadas automaticamente em aproximadamente dois segundos. Proporção 2:1 é recomendada, mas o importador antigo continua aceitando outras proporções.

**Novo:** PNGs terminados em `.cube.png` são interpretados como **atlas 3×2** de seis faces quadradas, nesta ordem:

```text
+-------------------+-------------------+-------------------+
| +X (East)         | -X (West)         | +Y (Up)           |
+-------------------+-------------------+-------------------+
| -Y (Down)         | +Z (South)        | -Z (North)        |
+-------------------+-------------------+-------------------+
```

Cada face deve medir **64..1024 px**, sempre quadrada. Exemplos válidos: 192×128 (64 px por face), 1536×1024 (512 px por face) e 3072×2048 (1024 px por face). Salve como `meuceu.cube.png` e selecione esse nome no World Studio. O programa mantém o atlas em uma única textura para evitar seis uploads.

Use o botão **Sky options** do World Studio para ajustar:

- **Skybox yaw**: rotação local da textura importada, entre 0 e 360 graus.
- **Drift deg/sec**: movimento lento do céu entre 0 e 2 graus por segundo (desligado por padrão).
- **PNG brightness**: iluminação da textura entre 20% e 100%.
- **Twinkling stars**: pequena variação do brilho das estrelas procedurais (desligado por padrão).

As opções acompanham perfis JSON; `Reset skybox options` volta aos defaults sem remover o PNG escolhido.

Os limites do importador permanecem: 16 MiB por PNG, largura 2..4096 e altura 2..2048; atlas também precisa ter células quadradas. Não são aceitos diretórios, URLs ou caminhos que escapem da pasta. Apague o nome do arquivo para restaurar o céu procedural. Use recursos próprios ou com licença apropriada.

Esta implementação não suporta seis arquivos independentes, HDR/EXR nem formatos de skyboxes de mods modernos. A orientação das faces e possíveis costuras precisam de verificação visual em execução real no Minecraft.

Aplicar uma paleta **Crystal**, **Sakura** ou **Nebula** em Atmosphere presets limpa a seleção de PNG para usar o céu procedural correspondente. O arquivo no disco é preservado; basta selecionar seu nome novamente para voltar ao panorama importado.

## Temas

No menu Appearance, salve um tema usando um identificador de até 40 caracteres. Exportar escreve `themes/exports/<nome>.json`; para importar, copie para `themes/imports/<nome>.json` e use Import theme. Os temas locais salvos ficam em `themes/<nome>.json`.

```json
{
  "version": 1,
  "accentColor": -5733633,
  "opacity": 0.94,
  "animationSpeed": 1.0,
  "animations": true,
  "rainbowAccent": false
}
```

`accentColor` é um inteiro ARGB Java. A interface também permite editar canais RGB diretamente. Limite: 64 KiB, profundidade JSON 16. O tema altera somente cor, transparência e opções de animação da interface; não troca o layout Panels/Studio, posições das colunas, HUD, emblemas ou efeitos de atmosfera. Esses dados pertencem aos perfis completos.

## Perfis completos

Salve o perfil primeiro no menu Profiles. Exportar escreve `exports/<nome>.json`. Importar lê `imports/<nome>.json`, valida e salva em `profiles/<nome>.json`, sem ativá-lo nem sobrescrever um perfil existente. Use Load profile para aplicar. Identificadores: 1..48 caracteres alfanuméricos, `_` ou `-`; a interface limita a 40.

Perfis incluem todas as seções de configuração. Limite: 256 KiB, profundidade JSON 32, versão 1. Valores fora de faixa são normalizados e seções ausentes recebem padrões. Versões desconhecidas e dados inválidos são rejeitados antes de substituir a configuração ativa. Arquivos/diretórios simbólicos não são aceitos no armazenamento de perfis/temas.

A versão do formato de perfil continua sendo **1**, mesmo no mod 0.2.0. Novos campos incluem `menuLayout`, posições/recolhimento em `panels`, widgets `brand` e `modules`, opções de movimento/emblema da capa e forma/cor secundária/partículas ambientes. Perfis antigos recebem padrões para campos ausentes, preservando as posições dos widgets já existentes. Para usar o novo HUD compacto, aplique **Compact preset** explicitamente no editor.

`config.json` guarda os padrões globais. `servers.json` guarda somente associações locais de endereço do servidor/mundo para nome de perfil. Durante uma associação ativa, autosave escreve no perfil; ao sair, restaura os padrões globais. A escolha global de habilitar perfis por servidor prevalece sobre a opção contida no arquivo importado.

## Atalhos por módulo na revisão de arquitetura

`moduleKeys` é um mapa de IDs para códigos de teclado 0–255, com zero significando sem tecla. O mapa recebe no máximo 64 entradas válidas e não ativa módulos desconhecidos. As configurações de teclas acompanham o perfil completo; perfis antigos recebem um mapa vazio. Configure os nove atalhos registrados em Options → Controls → Alien Modules.
