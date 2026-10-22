# Alien Client Unified — interface 0.4

Integração experimental em `integration/unified-redesign-0.4-20261022`, baseada na Unified da `main`. Reúne o redesign [#8](https://github.com/AttackAlien/Alien-Client/pull/8), as funcionalidades já incorporadas dos PRs #3–#5 e as melhorias dos #6/#7. **Esta branch não foi mesclada na `main`.**

**[Capturas reais do Minecraft](captures/README.md)** · **[Auditoria dos PRs e validação da integração](docs/INTEGRATION-0.4.md)** · [Instalação Unified](docs/PRIMEIRO-TESTE-UNIFIED.md)

Mod para Minecraft Java **1.8.9 / Forge 11.15.1.2318**, com Java 8. Um único `AlienClient-Unified-1.8.9.jar` inclui HUD, cosméticos 3D, céu/clima local, partículas e Combat Studio configurável. O combate começa desativado; presets LEGIT/FULL não garantem permissão em servidores. Fake anti-aim e silent aim não estão implementados.

## Interface e funções

- ClickGUI com pesquisa, favoritos, filtros e atalhos por módulo; animações PanelStudio MIT preservadas no sistema visual Alien.
- Componentes arredondados, Open Sans antialias, temas, HSV/RGBA/HEX, escala e blur opcional real.
- Cosmetic Studio com prévia 3D em um mundo, capas animadas, malhas curvas, quatro looks e três slots salvos, transformações e ajustes por jogador.
- World Studio com paletas, panorama/cubemap 3×2, galeria PNG local, favoritos, rotação/brilho, clima e partículas locais.
- HUD Studio com arraste, escala, alinhamento, fonte/cores, import/export e widgets de desempenho, teclas, inventário e módulos.
- Combat Studio preserva OFF/LEGIT/FULL/CUSTOM, mira gradual e cliques enquanto ataque é segurado, com configurações por perfil.

Right Shift abre o ClickGUI. Selecione um módulo para configurar atalho ou abrir seu editor; Settings dá acesso a perfis e preferências. Perfis Unified ficam em `.minecraft/config/alien-client/`. Instale só um JAR Alien por vez.

## Compilar e testar

Linux x86_64 com Bash, curl, tar, unzip, sha256sum e Python 3.9+:

```bash
./scripts/bootstrap.sh
./gradlew --no-daemon setupDecompWorkspace clean build # Unified padrão
ALIEN_EDITION=unified ./scripts/smoke-client.sh
```

O bootstrap verifica os SHA-256 do Temurin **8u472** e Gradle **2.14.1**; ForgeGradle 2.1 usa mappings stable_22 e TLS verificado. Neste ambiente, use `export ALIEN_TOOLS_DIR=/workspace/.alien-tools` antes dos comandos. Saída: `build/unified/libs/AlienClient-Unified-1.8.9.jar`; JUnit em `build/unified/reports/tests/index.html`.

Para executar interativamente: `./gradlew --no-daemon runClient`. O smoke usa um mundo e diretório descartáveis e captura o framebuffer real. As verificações de cubemap/combate e o empacotamento estão documentados no [relatório da integração](docs/INTEGRATION-0.4.md).

Regressão de composições históricas: `./gradlew --no-daemon -Pedition=legit clean build` e `-Pedition=full clean build`. Essas composições não substituem o build Unified.

No Windows, instale JDK 8 e Forge 1.8.9, configure `JAVA_HOME` e use `gradlew.bat setupDecompWorkspace build`, ou copie o JAR Unified validado para `mods`. O teste Windows de ZIP/integridade não comprova execução gráfica do Minecraft.

## Documentação e versões

[Redesign](docs/UI-OVERHAUL.md) · [Unified e migração](docs/UNIFIED-CLIENT.md) · [Studios V2](docs/STUDIOS-V2.md) · [Sky Ultimate](docs/SKY_STUDIO_ULTIMATE.md) · [Imports](docs/IMPORTS.md) · [Licenças](docs/THIRD_PARTY.md) · [Desenvolvimento](docs/DEVELOPMENT.md)

Releases anteriores permanecem em [downloads](downloads/README.md) e nas tags correspondentes. Os JARs 0.4 de Legit/Full são prévias anteriores à integração Unified. Consulte o relatório desta branch para distinguir os resultados atuais dos históricos.
