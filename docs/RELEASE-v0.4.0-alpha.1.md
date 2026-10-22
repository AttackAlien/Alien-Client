# Alien Client 0.4.0-alpha.1 — interface redesenhada

**[VER AS IMAGENS NOVAS](https://github.com/AttackAlien/Alien-Client/blob/v0.4.0-alpha.1/captures/README.md)** — capturas reais do Minecraft Forge 1.8.9, sem mockups ou retoques. As imagens antigas foram removidas da branch principal; os tags anteriores preservam o histórico.

## Baixar

- **[AlienClient-Legit-1.8.9.jar](https://github.com/AttackAlien/Alien-Client/blob/v0.4.0-alpha.1/downloads/v0.4.0-alpha.1/AlienClient-Legit-1.8.9.jar?raw=true)**
- **[AlienClient-Full-1.8.9.jar](https://github.com/AttackAlien/Alien-Client/blob/v0.4.0-alpha.1/downloads/v0.4.0-alpha.1/AlienClient-Full-1.8.9.jar?raw=true)**

Os links baixam os binários verificados versionados em downloads. [SHA-256](https://github.com/AttackAlien/Alien-Client/blob/v0.4.0-alpha.1/downloads/v0.4.0-alpha.1/SHA256SUMS.txt) · [Código-fonte ZIP](https://github.com/AttackAlien/Alien-Client/archive/refs/tags/v0.4.0-alpha.1.zip).

Minecraft Java 1.8.9, Forge 11.15.1.2318 e Java 8. Escolha um único JAR para mods, removendo a versão anterior. Right Shift abre o ClickGUI. A Full ainda é uma prévia de arquitetura, sem módulos avançados de combate/movimento/ESP/automação.

## Redesign

Open Sans suavizada, componentes próprios arredondados, ícones originais, sombras, temas persistentes e blur opcional real. ClickGUI com sidebar, pesquisa, filtros, favoritos e editor de atalhos. Cosmetic Studio com viewport maior e miniaturas de malhas reais; World Studio com galeria de paletas e controles; HUD Studio com estilo e import/export; Theme Studio com HSV/RGBA/HEX e seis presets. Sistemas visuais, perfis e a separação dos builds foram preservados.

## Validação

Cada edição passou em **101 testes JUnit e 124 verificações gráficas**, com 44 PNGs reais por execução. Foram exercitadas proporções 16:9/16:10/4:3, escala de menu, fonte suavizada, shaders do blur, canvas HSV, atalhos e os editores no mundo. Os JARs foram recompilados com clean build e inspecionados.

Os testes gráficos usaram classes atuais de desenvolvimento, Xvfb/OpenGL por software e limite de 30 FPS. Sem benchmark ou validação de launcher externo, GPU real, Windows, OptiFine, áudio ou multiplayer. [Relatório completo e ocorrências](https://github.com/AttackAlien/Alien-Client/blob/v0.4.0-alpha.1/docs/VALIDATION.md) · [Controles e limites](https://github.com/AttackAlien/Alien-Client/blob/v0.4.0-alpha.1/docs/UI-OVERHAUL.md).
