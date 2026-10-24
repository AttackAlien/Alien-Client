> **Candidato substituído.** Foi identificado um problema de apresentação da janela em menus. A correção e as novas capturas estão sendo validadas para a **0.6.0-alpha.2** no PR #18. Use o candidato indicado na conclusão do PR.

# Alien Interactive Studio 0.6.0-alpha.1 — candidato do PR #18

**Minecraft Java 1.8.9 / Forge 11.15.1.2318 / Java 8**. A versão do jogo foi mantida.

[Baixar ZIP](AlienClient-Unified-Primeiro-Teste.zip) · [Baixar JAR](AlienClient-Unified-1.8.9.jar) · [Instalação](LEIA-ME-INSTALACAO.txt) · [SHA-256](SHA256SUMS.txt)

Menu interativo, Orbit/galeria/perfis, céus e novos efeitos em um único JAR, preservando HUD/Full, partículas, capas/trilhas e Scene Studio. Fonte `8bde0c5d3a97ebc30b3ff0c5eeaee34535cfab57`. Candidato experimental, sem release estável e sem merge na main.

**175 JUnit por composição, 509 checks gráficos reais, 11 testes de empacotamento**. [Resultados/limites](../../docs/INTERACTIVE-STUDIO-VALIDATION.md) · [Recursos e uso](../../docs/INTERACTIVE-STUDIO.md) · [153 fotos](../../captures/README.md) · [Vídeo/GIF](../../captures/video-atual/README.md).

Remova o JAR Alien anterior da pasta mods e instale somente este, sem duplicar o modid. Não é APK/Bedrock. Menu → Module Gallery → Config library / Orbit; Effects Studio → Scene Studio → Sky styles. Shaders/framebuffer são necessários para os efeitos de composição. Perfis continuam locais, com compartilhamento manual do JSON.
