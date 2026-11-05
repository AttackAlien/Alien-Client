# Vídeo atual — Now Playing no Forge

**[Assistir / baixar MP4](Alien-Client-0.12-now-playing-fixture.mp4)** — 26,07 segundos, 1280×720, H.264, sem áudio. Gravação contínua da janela real do Minecraft Forge 1.8.9 em Linux/Mesa por software, por FFmpeg x11grab. Sem aceleração, montagem de fotos ou quadros interpolados.

**Os metadados deste vídeo vêm de uma fonte sintética de diagnóstico**, identificada no próprio HUD como “MÍDIA DE TESTE”. Não é uma reprodução do Spotify ou YouTube. A leitura nativa de sessões reais do Windows foi testada separadamente com um publicador SMTC de diagnóstico: [resultados](../../docs/NOW-PLAYING-VALIDATION.md).

[![Frame original extraído aos 4 segundos](preview-do-video.png)](Alien-Client-0.12-now-playing-fixture.mp4)

A sessão mostra Card, pausa, Transparent, arrastar/alinhamento no editor, opções, escala, troca de capa, ausência de duração, título longo e desconexão. A gravação começa após a primeira prévia Compact; as três aparências também têm [capturas do framebuffer](../README.md).

Captura solicitada a 30 FPS; taxa média codificada original **30,00 FPS** (`30/1`). Não é uma medição de desempenho em hardware físico. A janela X11 foi posicionada e mapeada antes da captura, sem modificar os pixels do jogo.

Fonte `28053d5fae099f7654d0ce47fea6d4b68e6566bb`. [Proveniência e comandos](../../docs/now-playing-results/2026-11-05/video-provenance.json) · [FFprobe](../../docs/now-playing-results/2026-11-05/video-ffprobe.json) · [SHA-256](SHA256SUMS.txt) · [Como usar o HUD](../../docs/NOW-PLAYING.md).

A mídia anterior saiu desta pasta atual. O filme completo de customizações e Full da versão 0.11 continua no [histórico Git](https://github.com/AttackAlien/Alien-Client/blob/372c4b98ffe37d1d81032f154e8ebcb952069a39/captures/video-atual/README.md).
