# Alien Client — interface 0.4.0-alpha.1

Personalização local para **Minecraft Java 1.8.9 / Forge 11.15.1.2318 / Java 8**, com builds Legit e Full separados. A Full continua como prévia de arquitetura: módulos avançados de combate, movimento, ESP e automação ainda não estão implementados.

**[VER AS IMAGENS NOVAS DO REDESIGN](captures/README.md)** · [Baixar Legit ou Full](downloads/v0.4.0-alpha.1/README.md) · [Release](https://github.com/AttackAlien/Alien-Client/releases/tag/v0.4.0-alpha.1)

![Menu real do novo Alien Client](captures/atual/legit/01-title.png)

A galeria atual contém somente capturas do redesign 0.4, obtidas no Minecraft/Forge real. As imagens antigas foram removidas da branch principal. Tags/releases anteriores preservam seu histórico.

## O que mudou

Fonte Open Sans suavizada, painéis arredondados, sombras, componentes próprios, ícones vetoriais, seis temas persistentes, seletor HSV/RGBA/HEX e desfoque opcional real. O ClickGUI ganhou sidebar, pesquisa, filtros, favoritos e editor de atalhos. Cosmetic Studio, World Studio, HUD Studio, temas, configurações e efeitos usam o mesmo sistema visual.

Os motores existentes foram preservados: cosméticos curvos, capa animada, prévia 3D/UUID, céu/PNG, clima local, partículas, HUD e perfis. Veja [detalhes do redesign e seus limites](docs/UI-OVERHAUL.md), [arquitetura das edições](docs/EDITIONS.md) e [validação](docs/VALIDATION.md).

## Instalar e usar

1. Instale Minecraft Java 1.8.9, Forge 11.15.1.2318 e Java 8.
2. Baixe **um único** JAR Legit ou Full e coloque em `mods`, removendo a versão Alien anterior. Os dois têm o mesmo ID Forge e não devem ser carregados juntos.
3. Entre em um mundo e pressione **Right Shift**. Clique em um cartão para ver detalhes/editar a tecla; a estrela marca favoritos. Settings abre preferências/perfis e Themes abre o Theme Studio.
4. As configurações Legit ficam em `config/alien-client/`; Full usa `config/alien-client-full/`. [Formatos de importação](docs/IMPORTS.md).

No Cosmetic Studio, arraste a prévia para girar e use a roda para zoom. No HUD Studio, arraste/redimensione widgets; Shift ignora o snap. O Theme Studio compartilha tipografia, cores, transparência, raio, sombras e movimento entre os menus. Blur começa desligado; reduzir animações preserva trabalho gráfico.

## Compilar e validar

Linux x86_64 com bash, curl, tar, unzip, sha256sum e Python 3.9+:

```bash
./scripts/bootstrap.sh
export ALIEN_TOOLS_DIR=/workspace/.alien-tools
./gradlew --no-daemon -Pedition=legit setupDecompWorkspace clean build
./gradlew --no-daemon -Pedition=full setupDecompWorkspace clean build
env -u DISPLAY ALIEN_EDITION=legit ./scripts/smoke-client.sh
env -u DISPLAY ALIEN_EDITION=full ./scripts/smoke-client.sh
```

O bootstrap verifica Temurin 8u472 e Gradle 2.14.1. ForgeGradle 2.1 usa mappings stable_22 e TLS verificado. Em uma máquina local, omita o export para usar `.tools/`. Não use Java 17/21 nesse ForgeGradle legado.

`./scripts/build-editions.sh` executa clean builds, inspeção dos binários e smoke gráfico das duas composições antes de copiar os JARs para `build/releases/`. Saídas individuais: `build/<edition>/libs/AlienClient-<Legit|Full>-1.8.9.jar`, relatórios em `build/<edition>/reports/tests/`, screenshots/resultados em `build/<edition>/smoke/`.

Com sessão gráfica local: `./gradlew -Pedition=legit runClient`, ou selecione full. Windows requer JDK 8 e `gradlew.bat -Pedition=legit setupDecompWorkspace clean build`; essa execução não foi validada nesta máquina Linux. O formato continua sendo mod Forge compatível com um futuro launcher; não há executável/launcher independente implementado.

[Desenvolvimento](docs/DEVELOPMENT.md) · [Dependências e licenças](docs/THIRD_PARTY.md) · [Auditoria/master plan](docs/ARCHITECTURE-AUDIT.md) · [Protocolo de benchmark](docs/BENCHMARKS.md).
