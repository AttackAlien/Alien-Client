# Unified 0.4.0-alpha.3 — candidato do PR #15

Build experimental da branch `feature/hud-studio-ultimate-20261023`, fonte `18c84442ffab1cdbd84d70bec3d7b9e50c2c56ea`. Não é um release estável nem um merge na main. HUD Ultimate, Full e Particle Studio estão no mesmo JAR.

- [Baixar ZIP de teste](AlienClient-Unified-Primeiro-Teste.zip)
- [Baixar JAR](AlienClient-Unified-1.8.9.jar)
- [Instalação](LEIA-ME-INSTALACAO.txt) · [SHA-256](SHA256SUMS.txt)
- [Relatório completo](../../docs/HUD-ULTIMATE-VALIDATION.md) · [Fotos reais](../../captures/README.md)

Requer Minecraft **Java 1.8.9**, Forge **11.15.1.2318** e **Java 8**. Use apenas um JAR Alien na pasta mods; remova o anterior dessa pasta antes de testar. Este mod não é um APK para Minecraft Bedrock/Android.

155 JUnit por composição; 161 verificações gerais, 73 HUD/Full/partículas, 37 céu e 24 combate; 11 testes do empacotador. Gameplay validado em Linux com OpenGL por software e servidor integrado descartável. Windows gameplay, GPU física e multiplayer externo ainda não foram testados. Anti Aim implementa giro real da câmera, sem fake angles/desync.
