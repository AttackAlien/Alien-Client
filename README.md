# Alien Client

> **Novo desenvolvimento (experimental): Alien Client Unified.** Em vez de instalar duas edições separadas, esta branch gera um único arquivo `AlienClient-Unified-1.8.9.jar` com perfis de combate OFF/LEGIT/FULL/CUSTOM configuráveis em jogo. A `main` e as versões publicadas ainda usam o sistema legado Legit/Full; veja [implementação, limites e migração](docs/UNIFIED-CLIENT.md). **Não misture os JARs no mesmo Forge.** Recursos como fake anti-aim/silent aim não estão implementados.

Mod de personalização **local** para Minecraft Java **1.8.9 / Forge 11.15.1.2318**, escrito em Java 8. Menu, HUD, acessórios 3D, céus e efeitos com identidade visual própria. Não exige launcher independente.

A base visual **0.2.0** foi preservada. O desenvolvimento **0.3.0-alpha.1** inicia a separação **Legit/Full** pedida no novo plano: [auditoria](docs/ARCHITECTURE-AUDIT.md), [arquitetura das edições](docs/EDITIONS.md) e [master plan](docs/MASTER-PLAN.md). A Full é uma prévia de arquitetura; os módulos avançados ainda estão planejados. A base compartilhada implementa os estúdios abaixo; [escopo e limitações](docs/FEATURES.md) distingue o que funciona das extensões ainda pendentes. Não há funcionalidades de combate, automação, alteração de atributos ou sincronização de cosméticos pela rede.

## Baixar

**[Baixar Legit ou Full — 0.3.0-alpha.1](downloads/v0.3.0-alpha.1/README.md)** · [70 capturas reais das duas edições](captures/editions/README.md) · [Testes e comandos](docs/VALIDATION-EDITIONS.md). **A Full é uma prévia de arquitetura, sem módulos avançados implementados.** Instale uma edição por vez.

Base visual anterior:

**[Baixar Alien Client 0.2.0.jar](https://github.com/AttackAlien/Alien-Client/blob/v0.2.0/downloads/alien-client-0.2.0.jar?raw=true)** · [Página da release](https://github.com/AttackAlien/Alien-Client/releases/tag/v0.2.0) — pré-release para testes.

Baixe o JAR pelo link acima e coloque em `mods`. Requer **Minecraft Java 1.8.9, Forge 11.15.1.2318 e Java 8**. O binário está versionado na pasta [downloads](downloads/), com SHA-256. O projeto para desenvolvimento está em [Source code (ZIP)](https://github.com/AttackAlien/Alien-Client/archive/refs/tags/v0.2.0.zip).

**[Ver as capturas reais e a capa em movimento](captures/README.md)** · [Resultados e limitações da validação](docs/VALIDATION.md)

## Compilar e testar

Linux x86_64, com `bash`, `curl`, `tar`, `unzip`, `sha256sum` e Python 3.9+:

```bash
./scripts/bootstrap.sh
./gradlew -Pedition=legit setupDecompWorkspace build
./gradlew -Pedition=full setupDecompWorkspace build
```

O bootstrap instala Temurin **8u472** e Gradle **2.14.1** em `.tools/`, verificando SHA-256. O projeto usa **ForgeGradle 2.1**, mappings **stable_22** e TLS verificado. Não use Java 17/21 para executar esse ForgeGradle legado. A preparação baixa os arquivos originais do Minecraft; nenhuma cópia é incluída no mod.

Neste ambiente preparado, as ferramentas ficam fora do checkout:

```bash
export ALIEN_TOOLS_DIR=/workspace/.alien-tools
./gradlew -Pedition=legit build
./gradlew -Pedition=full build
```

Saídas:

- Mods reofuscados: `build/legit/libs/AlienClient-Legit-1.8.9.jar` e `build/full/libs/AlienClient-Full-1.8.9.jar`; instale um por vez.
- Relatórios: `build/legit/reports/tests/index.html` e `build/full/reports/tests/index.html`.
- Cliente gráfico: `./gradlew -Pedition=legit runClient` ou `-Pedition=full runClient`.
- Validação completa das duas composições: `./scripts/build-editions.sh`; após aprovação, os dois binários ficam em `build/releases/`.

No Windows, instale um JDK 8, configure `JAVA_HOME` e execute `gradlew.bat -Pedition=legit setupDecompWorkspace build`. O wrapper verifica o SHA-256 da distribuição Gradle. A execução Windows não foi validada nesta máquina Linux.

## Instalar e usar

1. Instale o Forge **1.8.9-11.15.1.2318** no Minecraft Java 1.8.9 e use Java 8.
2. Copie **somente** `alien-client-0.2.0.jar` para a pasta `mods` dessa instalação.
3. Entre em um mundo e pressione **Right Shift**. O atalho pode ser alterado no menu Alien ou nos controles do Minecraft.
4. Use os painéis flutuantes; arraste os cabeçalhos, role cada coluna e clique com o botão direito para abrir seu editor. **Settings** abre o menu Studio e permite alternar entre os dois layouts. As preferências ficam em `.minecraft/config/alien-client/`.

Atalhos individuais dos nove interruptores principais podem ser definidos em **Options → Controls → Alien Modules** na revisão de arquitetura. Começam sem tecla e são salvos por perfil. A Full usa `config/alien-client-full/`, separado da Legit.

O menu inicial Alien é opcional: desative **Appearance → Home menu** para voltar ao menu padrão. O botão **Mods** continua disponível. Efeitos, céu e alterações de clima começam desativados; o HUD começa ativado.

## Editores

- **HUD Editor:** selecione e arraste os elementos; use a alça/canto ou a roda para redimensionar. A grade e os botões de alinhamento ajudam a organizar o layout. Há FPS, CPS esquerdo/direito, teclas, coordenadas, ping, relógio, inventário, armadura, memória/frame time, texto, marca Alien e lista dos efeitos visuais ativos. O **Compact preset** organiza o HUD branco sem fundo.
- **Cosmetic Studio:** escolha chapéus curvos, máscara/óculos, asas, mochila, charm ou capa de tecido com emblema original. Arraste a prévia para girar em 360° e use a roda para zoom. Fit controla posição, rotação e escala; Motion ajusta ondulação, tamanho e detalhe da capa. Players seleciona jogadores presentes e preferências por UUID. A prévia 3D exige um jogador carregado.
- **World Studio:** ative céu local, escolha um tema ou importe um panorama PNG, ajuste o horário visual e a neblina. Os presets **Crystal**, **Sakura** e **Nebula** combinam céu e partículas de diamantes, pétalas ou faíscas. Weather controla apenas a precipitação desenhada. O horário de iluminação dos blocos e as condições do servidor permanecem reais.
- **Effects Studio:** controle movimento, aterrissagem, trilhas, qualidade, distância e quantidade. Camera / color filter oferece rotação visual leve e uma sobreposição de cor opcional.
- **Appearance:** RGB, transparência, velocidade das animações, cores e biblioteca de temas.
- **Profiles:** salve, carregue, importe e exporte um conjunto completo. Para associar um perfil a um servidor, salve-o, entre no servidor e use **Bind current server**. Ao sair, a configuração global é restaurada; alterações no perfil do servidor não sobrescrevem os padrões globais.

Nomes de perfis e temas usam letras, números, `_` e `-`. Veja os diretórios e formatos em [importações](docs/IMPORTS.md).

Os perfis **Competitive**, **Balanced** e **Cinematic** alteram os limites de efeitos Alien. Não pressupõem ganho de FPS e não modificam as opções gráficas vanilla. Use o HUD de desempenho e o [protocolo de benchmark](docs/BENCHMARKS.md) para medir sua máquina.

Veja também [arquitetura e desenvolvimento](docs/DEVELOPMENT.md), [validação](docs/VALIDATION.md) e [dependências e licenças](docs/THIRD_PARTY.md).
