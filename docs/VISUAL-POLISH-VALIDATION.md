# Validação — cosméticos e trilhas Unified 0.4.0-alpha.4

Fonte **`67b4830e00b9620e15b8177a5421cda00491be63`**, branch `feature/cosmetic-trails-polish-20261023`, [PR #16](https://github.com/AttackAlien/Alien-Client/pull/16). PR empilhado sobre #15, preservando seu HUD/Full e redesign. Nenhum merge na main. Sessões finais gravadas em 23/10/2026, 15:17–15:21 UTC.

| Verificação local | Resultado |
| --- | --- |
| Unified: clean build, JUnit e verificação do JAR | **160 testes**, zero falhas/erros/ignorados |
| Legit histórica: clean build, JUnit e JAR | **160 testes**, zero falhas/erros/ignorados |
| Full histórica: clean build, JUnit e JAR | **160 testes**, zero falhas/erros/ignorados |
| Forge real: interface/cosméticos/perfis/regressões | **PASS 161 checks** |
| Forge real: HUD/Full/partículas/cosméticos refinados | **PASS 92 checks** |
| Forge real: Sky Studio, cubemap e restauração | **PASS 37 checks** |
| Forge real: combate anterior e perfis/sessão | **PASS 24 checks** |
| Empacotamento e rejeição de evidência inválida/antiga | **11 testes Python aprovados** |
| Empacotamento do JAR/ZIP experimental | **PASS** |

São 480 execuções JUnit (160 por composição) e **314 verificações gráficas**; incluem verificações de captura do framebuffer, controles, persistência e estado real do jogo, não apenas comparações visuais. Não é um benchmark de FPS. As composições históricas foram compiladas/testadas; as quatro sessões gráficas acima são da Unified.

## O que foi realmente validado

- Trilha Ribbon emitida por movimento real via teclado X11, com segmentos desenhados dentro do limite do Performance Engine. Mudança de direção/câmera frontal mantém o desenho. Parar expira a trilha; desativar animações limpa a simulação. O estilo clássico Particles permanece selecionável, com sliders exclusivos de Ribbon desabilitados.
- Inércia da capa reage ao movimento real do jogador e usa estado independente por jogador; a prévia tem estado separado. A malha mantém ombros presos, UVs estáveis e borda arredondada. A resposta é uma mola amortecida aplicada à malha; não é uma simulação completa de tecido/colisão.
- Coroa Sakura com pétalas volumétricas, detalhes do kasa e tecido CC0 são renderizados no jogador e no Studio. Perfis/looks preservam os novos campos. O asset CC0 foi conferido por hash e seu crédito está incluído no JAR.
- HUD Ultimate, crosshair, teclas/CPS/armadura, editor, presets e os módulos Full anteriores continuam passando. Fome, cura por poção e bloco da ponte são confirmados no servidor integrado real. Alvos de combate são entidades client-side de treino.
- Após partículas, itens, texturas, novos hats e fitas, `glGetError()` retorna `GL_NO_ERROR`. Teleportes/expiração/limites e estabilidade da capa entre taxas de render são cobertos pelos novos testes unitários.

A primeira inspeção de capturas identificou coroa baixa e topo cortado pelo zoom, corrigidos antes das sessões finais. A orientação da fita passou a usar a base da câmera, incluindo terceira pessoa frontal. Os resultados finais foram executados novamente após essas alterações.

## Fotos, vídeo e build

[97 fotos atuais](../captures/README.md), [MP4 de 3min09s e GIF de 1min26s](../captures/video-atual/README.md), [resultados/proveniência das sessões](../captures/atual/unified/validation.json) e [JAR/ZIP experimental](../downloads/v0.4.0-alpha.4/). As mídias anteriores foram substituídas nos mesmos diretórios atuais; permanecem apenas no histórico Git.

O MP4 concatena quatro gravações reais X11, em velocidade normal, 1280×720/20 fps, sem áudio. Preserva carregamentos e mudanças de resolução feitas pelos testes. O GIF seleciona 29 trechos reais, com tamanho/cores/fps reduzidos, sem interpolação de frames. Os PNGs são framebuffers originais, sem retoques.

A fonte externa efetivamente incluída é um mapa de tecido **CC0 de Rob Tuytel / Poly Haven**. Referências de repositórios e limites de licenças/compatibilidade estão em [VISUAL-POLISH-REFERENCES.md](VISUAL-POLISH-REFERENCES.md). Nenhum código WaveyCapes, three.js, Luna’s Cosmetics ou PlayerAnimator foi incluído.

```text
JAR SHA-256: 4efb1df3fc55886ee6f44b47747d0e93de130f2e1dd17459747a89128c34b2f5
Fontes/build SHA-256: 2bed0948bdf88ef5178d28a96327a6d03705d71fb4ec654cf8f22af36f7b0e04
Classes/recursos SHA-256: ba43fd41e05ab1e1745c367c5dfec5d45782a1e2ecbb37c45e872f0e7ad9dabf
JUnit SHA-256: 7fe5a36bdb4ade415d1a33800825084b88a74272818b06da102cacfb7dc7ca6b
```

A janela de validação foi aberta depois do build e antes da sessão de combate. JAR, fontes, classes/recursos e relatórios JUnit continuaram iguais até o empacotamento. O candidato distribuído é byte a byte igual ao JAR verificado; não inclui os mods auxiliares de testes.

## Comandos utilizados

JDK 8u472, Gradle 2.14.1, Forge 11.15.1.2318/stable_22, Linux, Xvfb e Mesa por software. Builds sequenciais:

```bash
export ALIEN_TOOLS_DIR=/workspace/.alien-tools
for edition in unified legit full; do
  ./gradlew --no-daemon "-Pedition=$edition" clean build \
    smokeClasses skySmokeClasses combatSmokeClasses ultimateSmokeClasses
done

# Xvfb :94 1280x720x24, GLX e LIBGL_ALWAYS_SOFTWARE=1.
# Cada tarefa foi gravada por FFmpeg x11grab em uma sessão própria:
./gradlew --no-daemon -I /tmp/alien-video-size.gradle -Pedition=unified runSmokeClient
./gradlew --no-daemon -I /tmp/alien-video-size.gradle -Pedition=unified runSkySmokeClient
python3 scripts/package-unified.py --begin-validation
./gradlew --no-daemon -I /tmp/alien-video-size.gradle -Pedition=unified runCombatSmokeClient
./gradlew --no-daemon -I /tmp/alien-video-size.gradle -Pedition=unified runUltimateSmokeClient
python3 -m unittest discover -s scripts/tests -p test_package_unified.py -v
GITHUB_SHA=67b4830e00b9620e15b8177a5421cda00491be63 python3 scripts/package-unified.py
```

O init script temporário apenas acrescentou largura/altura iniciais de 1280×720; não mudou o código do client. Detalhes de gravação e capítulos estão no [README da mídia](../captures/video-atual/README.md).

CI da revisão de código **aprovado**, disparado manualmente na branch: [Unified/Windows integridade](https://github.com/AttackAlien/Alien-Client/actions/runs/39976946378) e [Forge Legit/Full](https://github.com/AttackAlien/Alien-Client/actions/runs/39976951833). Os quatro checks passaram: Unified Java 8, Windows ZIP/JAR, Forge Legit e Forge Full. Isso se refere à revisão de código indicada acima; o commit posterior publica apenas evidências/documentação/binários já validados.

## Limites

Gameplay validado em Linux/Mesa e servidor integrado descartável; Windows gameplay, OptiFine, GPU física, input humano e multiplayer externo não foram testados. O check Windows verifica ZIP/JAR, não o jogo. O atlas de cubemap é uma imagem técnica de teste renderizada no jogo. Não há render PBR, física completa de colisão de tecido, sincronização de cosméticos entre clients ou assets baixados durante a execução. As funções Full e seus limites continuam os documentados no [HUD Ultimate](HUD-ULTIMATE.md), incluindo Anti Aim como giro real, sem fake angles/desync.
