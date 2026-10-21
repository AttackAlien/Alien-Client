# Recursos e perfis locais

Diretório base: `<pasta do jogo>/config/alien-client/`. Crie os subdiretórios ao copiar os arquivos, se necessário. Recursos não são baixados automaticamente.

## Modelo de acessório

Coloque `meu-chapeu.json` em `cosmetics/`:

```json
{
  "boxes": [
    {"x": -2, "y": -11, "z": -2, "width": 4, "height": 3, "depth": 4}
  ]
}
```

As coordenadas são pixels do modelo Minecraft, com a cabeça como ponto de referência. Use de 1 a 128 caixas, posições entre -64 e 64 e dimensões inteiras entre 1 e 32. Limite de 1 MiB por arquivo. Na aba Imports do Cosmetic Studio, **Create example box model** cria um exemplo sem sobrescrever arquivos existentes. **Reload local assets** limpa o cache; selecione o arquivo com o botão Model.

A textura é opcional: PNG real em `cosmetics/`, de até 1024 × 1024 e 1 MiB. O layout UV do modelo usa uma base 64 × 64 compartilhada pelas caixas. Selecione-a com o botão Texture. A importação é para esse formato de caixas, não para formatos externos de modelagem.

Os recursos ficam em cache por nome, com no máximo 32 combinações de modelo/textura. Após editar um arquivo no disco, use Reload. Modelos inválidos são ignorados, com mensagem na aba Imports.

## Céu 360°

Coloque um PNG equiretangular em `skyboxes/` e informe apenas o nome, por exemplo `galaxia.png`, no World Studio. Ative Custom sky e use **Apply RGB / PNG**. O arquivo é recarregado após mudanças no disco, com verificação periódica de aproximadamente dois segundos.

Limites: 16 MiB, largura 2..4096 e altura 2..2048. Uma proporção 2:1 é recomendada para evitar distorção; o importador aceita outras proporções. Não são aceitos nomes com diretórios, URLs ou caminhos que escapem do diretório de importação. Apague o nome para voltar ao céu procedural. Use apenas recursos que você criou ou tem licença para utilizar.

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

`accentColor` é um inteiro ARGB Java. A interface também permite editar canais RGB diretamente. Limite: 64 KiB, profundidade JSON 16. O tema altera apenas aparência, sem ativar efeitos ou modificar o HUD.

## Perfis completos

Salve o perfil primeiro no menu Profiles. Exportar escreve `exports/<nome>.json`. Importar lê `imports/<nome>.json`, valida e salva em `profiles/<nome>.json`, sem ativá-lo nem sobrescrever um perfil existente. Use Load profile para aplicar. Identificadores: 1..48 caracteres alfanuméricos, `_` ou `-`; a interface limita a 40.

Perfis incluem todas as seções de configuração. Limite: 256 KiB, profundidade JSON 32, versão 1. Valores fora de faixa são normalizados e seções ausentes recebem padrões. Versões desconhecidas e dados inválidos são rejeitados antes de substituir a configuração ativa. Arquivos/diretórios simbólicos não são aceitos no armazenamento de perfis/temas.

`config.json` guarda os padrões globais. `servers.json` guarda somente associações locais de endereço do servidor/mundo para nome de perfil. Durante uma associação ativa, autosave escreve no perfil; ao sair, restaura os padrões globais. A escolha global de habilitar perfis por servidor prevalece sobre a opção contida no arquivo importado.
