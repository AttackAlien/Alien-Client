# Launcher 0.3.0-alpha.10 — Now Playing

**[Baixar Windows x64 — 75.29 MiB](https://github.com/AttackAlien/Alien-Client/raw/refs/heads/feature/now-playing-hud-20261105/downloads/launcher/v0.3.0-alpha.10/AlienLauncher-Windows-x64-0.3.0-alpha.10.zip)** · [Capturas reais](../../../captures/launcher-atual/README.md) · [Validação](../../../docs/NOW-PLAYING-VALIDATION.md) · [Hashes](SHA256SUMS.txt).

Inclui Unified 0.12.0-alpha.1 e a ponte local de metadados do Windows. Extraia o ZIP inteiro e mantenha `payload` ao lado de `AlienLauncher.exe`. Em Windows 10 1809+/11 x64, instale/repare o perfil Alien 1.8.9 e conecte sua conta Microsoft com Minecraft Java.

Abra **HUD Studio Ultimate → Now Playing → Toggle → Options**. Inicie a música no aplicativo. Não é necessário login do Spotify no client. Há três layouts, controles individuais de capa/artista/tempo/progresso, fonte, posição, escala, alinhamento, opacidade e cores nos perfis.

[CI 42053831389](https://github.com/AttackAlien/Alien-Client/actions/runs/42053831389) concluído com sucesso: 37 testes Linux, 37 Windows, 46 checks da janela nativa, seis capturas e instalação isolada real de Forge/Java 8. A ponte passou em nove checks de Windows 11 com publicador SMTC real de diagnóstico; Spotify/YouTube nos aplicativos reais ainda não foram validados.

Fonte do launcher `721fa8cd5bae384efc62a9b68d0c3957b1c45756`; JAR fixado da fonte `28053d5fae099f7654d0ce47fea6d4b68e6566bb`, CI `42051508518`. ZIP SHA-256 `7f37a88e6d6eb31fe45709536ab0408322b35b6e9494ddb319167a4b88eb3336`; JAR `d59f41b116011cf52cb7607f281e8e65218029e054162844f12f11911d43e5bf`. Pacote original do CI com CRC, EXE AMD64 e payload verificados.

Candidato do PR #29 sobre #28, sem merge ou alteração do release 0.10. [Guia](../../../docs/NOW-PLAYING.md).
