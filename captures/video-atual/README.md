# Vídeo atual — Alien Client Unified 0.4.0-alpha.3

**Gravação real da tela do Minecraft Forge 1.8.9**, feita em 23/10/2026, entre 14:37 e 14:41 UTC. Não é uma apresentação de imagens estáticas nem uma renderização simulada. Navegação automatizada pelos testes, em mundos descartáveis; quatro sessões concatenadas, com seus carregamentos e mudanças reais de resolução.

## Assistir ou baixar

- **[MP4 completo — 3min02s, 720p, 20 fps, aproximadamente 13 MB](https://raw.githubusercontent.com/AttackAlien/Alien-Client/refs/heads/feature/hud-studio-ultimate-20261023/captures/video-atual/Alien-Unified-tour-completo.mp4)**
- **[GIF — 1min03s, aproximadamente 3,6 MB](https://raw.githubusercontent.com/AttackAlien/Alien-Client/refs/heads/feature/hud-studio-ultimate-20261023/captures/video-atual/Alien-Unified-previa.gif)**
- [89 fotos atuais em tamanho original](../README.md) · [JAR/ZIP de teste](../../downloads/v0.4.0-alpha.3/) · [PR #15](https://github.com/AttackAlien/Alien-Client/pull/15)

Se o navegador baixar o MP4 em vez de abrir um player, abra o arquivo baixado no reprodutor de vídeo. O vídeo é sem áudio, sem aceleração e sem frames sintetizados. Pause para ler as telas: a navegação dos testes é rápida.

## Capítulos do MP4

| Início | Conteúdo |
| --- | --- |
| 00:00 | Menu inicial, módulos, cosméticos 3D, capa/asas, céu/clima, Performance Engine, temas, editor e perfis. |
| 01:11 | Sky Studio: cubemap técnico de teste, cores/orientação, controles, galeria e restauração do céu. |
| 01:45 | Combat Studio: configuração, mira/clique anteriores e perfis. |
| 02:07 | HUD Ultimate com avatar/HP animado, teclas/CPS/armadura/mira, editor/presets, Auto Food/Potion/Bridge/Aimbot/Sprint/spin e novas partículas. |

Os capítulos marcam o início de cada sessão, incluindo seu carregamento. As cenas de cubemap mostram um atlas técnico de cores preparado para testar o renderizador, identificado no teste; não são um pacote artístico de céu. Os alvos são entidades client-side de treino; comida, cura e bloco da ponte são conferidos no servidor integrado real. Não houve conexão a servidor externo. Anti Aim é giro real da câmera, sem fake angles/desync.

## GIF de prévia

21 trechos reais do mesmo vídeo, com cortes secos, em velocidade normal, reduzidos para 480 px e 5 fps para caber no celular. A qualidade completa está no MP4.

![Prévia real de 1min03s](Alien-Unified-previa.gif)

## Proveniência e resultados

Revisão gravada `f25304750ac8d73d4c01d5ccf1d213c6083f0b59`; último commit de código `18c84442ffab1cdbd84d70bec3d7b9e50c2c56ea`. Código de produção e JAR não foram alterados para a gravação. As quatro sessões passaram novamente: [geral — 161](general-result.txt), [céu — 37](sky-result.txt), [combate — 24](combat-result.txt), [HUD/Full/partículas — 73](ultimate-result.txt).

[Dados técnicos, tempos e seleção dos trechos](proveniencia.json) · [SHA-256](SHA256SUMS.txt) · [Validação completa e limitações](../../docs/HUD-ULTIMATE-VALIDATION.md).

Captura com FFmpeg `x11grab`, Xvfb 1280×720 e Mesa por software, sem cursor sobreposto. O MP4 concatena os streams H.264 das quatro gravações sem recodificação. GIF usa somente esses frames, com redução de tamanho/cores e amostragem, sem interpolação. O jogo estava limitado a 30 FPS; os 20 fps do vídeo são a taxa de captura, não um benchmark.

Comandos centrais utilizados (display pronto e Minecraft em execução):

```bash
ffmpeg -f x11grab -draw_mouse 0 -framerate 20 -video_size 1280x720 \
  -i :94 -an -c:v libx264 -threads 2 -preset veryfast -crf 22 \
  -pix_fmt yuv420p -movflags +faststart sessao.mp4

# Cada tarefa foi executada uma vez durante a captura correspondente:
./gradlew --no-daemon -I /tmp/alien-video-size.gradle -Pedition=unified runSmokeClient
./gradlew --no-daemon -I /tmp/alien-video-size.gradle -Pedition=unified runSkySmokeClient
./gradlew --no-daemon -I /tmp/alien-video-size.gradle -Pedition=unified runCombatSmokeClient
./gradlew --no-daemon -I /tmp/alien-video-size.gradle -Pedition=unified runUltimateSmokeClient

ffmpeg -f concat -safe 0 -i concat.txt -f ffmetadata -i chapters.ffmeta \
  -map 0:v:0 -map_metadata 1 -map_chapters 1 -c copy \
  -movflags +faststart Alien-Unified-tour-completo.mp4
```

O init script temporário apenas acrescentou `--width 1280 --height 720` aos argumentos iniciais das tarefas; os testes continuam exercitando suas próprias mudanças de escala/resolução. Os caminhos temporários dos comandos são registros desta execução, não arquivos necessários para instalar o client.
