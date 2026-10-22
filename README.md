# Alien Client

> **Alien Client Unified agora é o build padrão na `main`.** Um único `AlienClient-Unified-1.8.9.jar` reúne os visuais, o Sky Studio Ultimate e perfis opcionais de combate **OFF / LEGIT / FULL / CUSTOM**. O JAR aprovado em CI é **experimental, não uma nova release oficial**. Veja [implementação, testes e migração](docs/UNIFIED-CLIENT.md). **Instale só um JAR Alien por vez; fake anti-aim e silent aim ainda não foram implementados.**

Mod cliente de personalização visual e opções de combate **locais/configuráveis** para Minecraft Java **1.8.9 / Forge 11.15.1.2318**, escrito em Java 8. Menu, HUD, acessórios 3D, céus e efeitos com identidade visual própria. Não exige launcher independente.

A base visual **0.2.0** foi preservada. A linha **0.3.0-alpha.1** criou builds históricos Legit/Full; a `main` passou a usar **Unified** após os PRs [#3](https://github.com/AttackAlien/Alien-Client/pull/3), [#4](https://github.com/AttackAlien/Alien-Client/pull/4) e [#5](https://github.com/AttackAlien/Alien-Client/pull/5). O [master plan](docs/MASTER-PLAN.md) continua como planejamento para módulos não entregues. A base inclui HUD, cosméticos, estúdios de céu/efeitos e, no Unified, assistências de mira/clique desativadas por padrão. Nada modifica o alcance no servidor ou sincroniza cosméticos pela rede.

## PanelStudio — base de integração já incorporada

O PR #3 incorporou a integração **incremental** de duas classes Java 8 do [PanelStudio](https://github.com/lukflug/PanelStudio) (MIT): animação por tempo real no hover e novos cantos suaves nos botões Alien, sem substituir os editores testados. **Isso ainda não é o ClickGUI PanelStudio completo.** Veja a [revisão de licenças e integração](docs/OPEN_SOURCE_REVIEW.md). A fonte e os demais layouts ainda precisam de atualização.

## Baixar

**[Baixar a build experimental do Unified (GitHub Actions)](https://github.com/AttackAlien/Alien-Client/actions/workflows/unified-client-ci.yml)** — selecione a execução verde mais recente da branch `main`, abra *Artifacts*, obtenha `alien-unified-<id>` e extraia `distribution/AlienClient-Unified-Primeiro-Teste.zip`. O ZIP reúne **um único JAR instalável**, instruções em português, SHA256 e validação. O acesso a artefatos do Actions pode exigir login no GitHub. Enquanto a nova automação não for aprovada na `main`, existe a [build integrada anterior validada](https://github.com/AttackAlien/Alien-Client/actions/runs/39809488208).

**[Guia passo a passo para instalar e testar no Windows](docs/PRIMEIRO-TESTE-UNIFIED.md)**. Este é um teste experimental, não uma release estável. Remova JARs antigos de `mods` antes de instalar.

**Versões históricas:** [Baixar Legit ou Full — 0.3.0-alpha.1](downloads/v0.3.0-alpha.1/README.md) · [70 capturas reais das duas edições](captures/editions/README.md) · [Testes e comandos](docs/VALIDATION-EDITIONS.md). **A Full é uma prévia de arquitetura, sem módulos avançados implementados.** Instale uma edição por vez.

Base visual anterior:

**[Baixar Alien Client 0.2.0.jar](https://github.com/AttackAlien/Alien-Client/blob/v0.2.0/downloads/alien-client-0.2.0.jar?raw=true)** · [Página da release](https://github.com/AttackAlien/Alien-Client/releases/tag/v0.2.0) — pré-release para testes.

Para instalar **Unified**, coloque somente `AlienClient-Unified-1.8.9.jar` em `mods`. Requer **Minecraft Java 1.8.9, Forge 11.15.1.2318 e Java 8**. Os binários em [downloads](downloads/) são versões **históricas**, não substituem o novo pacote experimental do Actions.

**[Ver as capturas reais e a capa em movimento](captures/README.md)** · [Resultados e limitações da validação](docs/VALIDATION.md)

## Compilar e testar

Linux x86_64, com `bash`, `curl`, `tar`, `unzip`, `sha256sum` e Python 3.9+:

```bash
./scripts/bootstrap.sh
./gradlew -Pedition=unified setupDecompWorkspace build

# Somente para regressão de edições antigas:
./gradlew -Pedition=legit setupDecompWorkspace build
./gradlew -Pedition=full setupDecompWorkspace build
```

O bootstrap instala Temurin **8u472** e Gradle **2.14.1** em `.tools/`, verificando SHA-256. O projeto usa **ForgeGradle 2.1**, mappings **stable_22** e TLS verificado. Não use Java 17/21 para executar esse ForgeGradle legado. A preparação baixa os arquivos originais do Minecraft; nenhuma cópia é incluída no mod.

Neste ambiente preparado, as ferramentas ficam fora do checkout:

```bash
export ALIEN_TOOLS_DIR=/workspace/.alien-tools
./gradlew -Pedition=unified build
```

Saídas:

- **JAR unificado reofuscado:** `build/unified/libs/AlienClient-Unified-1.8.9.jar`.
- Relatórios JUnit: `build/unified/reports/tests/index.html`; smoke de combate: `build/unified/combat-smoke/`.
- Cliente gráfico local: `./gradlew -Pedition=unified runClient`.
- **Pacote validado:** CI executa o smoke de Minecraft e depois `python3 scripts/package-unified.py`, gerando `build/unified/distribution/AlienClient-Unified-Primeiro-Teste.zip`.
- **Somente regressão histórica:** `scripts/build-editions.sh` compila builds Legit/Full antigos.

No Windows, instale um JDK 8, configure `JAVA_HOME` e execute `gradlew.bat -Pedition=unified setupDecompWorkspace build` se quiser compilar. Para **jogar**, não é necessário compilar: baixe o JAR verificado. O teste de instalação no Windows ainda depende de validação manual.

## Instalar e usar

1. Instale o Forge **1.8.9-11.15.1.2318** no Minecraft Java 1.8.9 e use Java 8.
2. Copie **somente** `AlienClient-Unified-1.8.9.jar` do pacote experimental aprovado para a pasta `mods` dessa instalação; remova outras versões Alien Client.
3. Entre em um mundo e pressione **Right Shift**. O atalho pode ser alterado no menu Alien ou nos controles do Minecraft.
4. Use os painéis flutuantes; arraste os cabeçalhos, role cada coluna e clique com o botão direito para abrir seu editor. **Settings** abre o menu Studio e permite alternar entre os dois layouts. As preferências ficam em `.minecraft/config/alien-client/`.

Atalhos individuais dos módulos estão em **Options → Controls → Alien Modules**, sem tecla por padrão. O Unified usa `config/alien-client/` e preserva perfis antigos da Legit; configurações da Full antiga em `config/alien-client-full/` exigem migração manual. O Combat Studio tem presets OFF / LEGIT / FULL / CUSTOM, com combate desligado inicialmente.

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
