# Validação — HUD Ultimate, Full e partículas / Unified

Revisão de código `18c84442ffab1cdbd84d70bec3d7b9e50c2c56ea`, versão **0.4.0-alpha.3**, na branch `feature/hud-studio-ultimate-20261023`. Build e sessões locais concluídos em **23/10/2026**, 14:17–14:21 UTC. [PR #15](https://github.com/AttackAlien/Alien-Client/pull/15), sem merge desta revisão.

A base funcional vem da main `933f93ef3498f204ee3858c78785fd4b7992d937`, com redesign 0.4, Performance Engine V2 e proteção de sessão. A atualização posterior do README da main foi incorporada em `8c9880c` e preservada. A [auditoria e implementação](HUD-ULTIMATE.md) registra os widgets/módulos anteriores e os limites dos novos recursos.

## Resultados finais locais

| Verificação | Resultado | Evidência |
| --- | --- | --- |
| Unified: clean build, JUnit, verificação do JAR | **155 testes**, zero falhas/erros/ignorados | [Resumo por edição](../captures/atual/unified/validation.json) |
| Legit: clean build, JUnit, verificação do JAR | **155 testes**, zero falhas/erros/ignorados | Mesmo resumo |
| Full histórica: clean build, JUnit, verificação do JAR | **155 testes**, zero falhas/erros/ignorados | Mesmo resumo |
| Unified: interface, cosméticos, perfis e regressões | **PASS 161 checks** | [Resultado](../captures/atual/unified/result.txt) |
| Unified: HUD Ultimate, Full e partículas | **PASS 73 checks** | [Resultado](../captures/atual/unified/result-ultimate.txt) |
| Unified: cubemap, galeria, restauração do céu | **PASS 37 checks** | [Resultado](../captures/atual/unified/result-sky.txt) |
| Unified: combate anterior e sessão/perfis | **PASS 24 checks** | [Resultado](../captures/atual/unified/result-combat.txt) |
| Empacotamento e rejeição de evidência inválida/antiga | **11 testes Python aprovados** | [Empacotador](../scripts/tests/test_package_unified.py) |
| Pacote instalável | **PASS**, JAR original sem alteração | [Candidato de teste](../downloads/v0.4.0-alpha.3/) |

São **465 execuções JUnit** (155 por composição, não 465 testes distintos) e **295 verificações gráficas** no Unified. As verificações gráficas incluem captura/dimensões do framebuffer, controles, estado do jogo, persistência e amostras de pixels. Não são 295 comparações golden image nem um benchmark de desempenho.

O CI da revisão de código também aprovou os seis checks: Forge Legit/Full, Sky Legit/Full, Unified Java 8 e integridade ZIP/JAR no Windows. [Execução Unified](https://github.com/AttackAlien/Alien-Client/actions/runs/39968363502), [Forge](https://github.com/AttackAlien/Alien-Client/actions/runs/39968363596), [Sky](https://github.com/AttackAlien/Alien-Client/actions/runs/39968363687). A publicação posterior de documentação/fotos pode gerar nova execução, cujo estado atual aparece no PR.

## Gravação posterior, sem alteração do código

Entre 14:37 e 14:41 UTC, as quatro sessões foram executadas novamente para gravar o [MP4 de 3min02s e o GIF de 1min03s](../captures/video-atual/README.md). Todas repetiram os mesmos resultados: 161, 37, 24 e 73 checks. As 89 fotos da galeria foram novamente substituídas pelos framebuffers dessas sessões. Não houve recompilação/alteração do código de produção ou do JAR; os testes JUnit e hashes acima continuam vinculados ao mesmo artefato. A revisão gravada é `f25304750ac8d73d4c01d5ccf1d213c6083f0b59`, cujo CI também aprovou os seis checks ([Unified e Windows integridade](https://github.com/AttackAlien/Alien-Client/actions/runs/39969649427)).

## O que foi exercitado no jogo

- **HUD:** os 12 widgets originais continuam registrados, somados aos três novos; mira ciano conferida no centro do framebuffer; Target HUD acompanha entidade real client-side, anima HP de 20 para 8 e limpa o alvo removido. Armadura vem do inventário sincronizado. CPS/teclas recebem eventos X11 automatizados entregues ao LWJGL.
- **Editor:** arraste e alça de resize pelos handlers reais da tela; alinhamento, opacidade, configurações do widget, presets, tema Light, escala de menu reduzida e persistência conjunta de HUD/Full.
- **Full:** fome aumenta no servidor integrado após comer; poção de beber e splash curam de verdade; splash mantém posse da câmera mesmo com Aimbot ligado e restaura o pitch; a ponte é um bloco real confirmado no servidor. Sprint e giro de câmera alteram estados reais. Abrir GUI/desligar master libera o uso de item e restaura o slot original. O preset Full habilita os extras implementados.
- **Partículas:** emissão real nos presets Cinematic/Sakura/PvP; geometrias e brilho desenhados no mundo com limites de 96/72/24, persistência de opacidade/movimento/duração e limpeza ao desligar. Estado OpenGL sem erros após render e controles. Os testes unitários cobrem fade/interpolação, geometria em cache com bordas transparentes, perfis antigos e entradas inválidas.
- **Regressões:** menus, cosméticos/capa em movimento, temas, perfis, Performance Engine e importação/restauração de céu. A suíte de combate continua verificando a assistência gradual anterior, separada do novo Aimbot instantâneo.

## Capturas e artefato

A [galeria atual](../captures/README.md) contém **89 PNGs novos e originais** das quatro sessões da mesma revisão. Substitui a coleção anterior; versões antigas só ficam no histórico Git. O GIF da capa usa os 12 frames originais sem criar frames intermediários. As amostras de cubemap usam um atlas técnico de cores renderizado dentro do jogo, identificado como teste.

[JAR e ZIP de teste](../downloads/v0.4.0-alpha.3/), candidato do PR, sem publicação de release estável. O JAR distribuído é byte a byte igual ao JAR compilado/verificado e não inclui os mods auxiliares de teste.

```text
JAR SHA-256:
e6131a1ddfef8731026eb4596b6805b9c3beb2291f2761a35e017940d6797a5f
Fontes/build SHA-256:
11bcf711e2b529405518ad9cc87077d13331d2e02ff4eed74d78261bc6e4606a
Classes/recursos SHA-256:
5f800ea57a97361c64064b0a44bd1e7939f49a701335168651808388ee597c48
Relatórios JUnit SHA-256:
3c67ea751908a8b67a11e3c272351df2c7526cb718276fabe50e225106fc2efc
```

[Manifesto das sessões e proveniência](../captures/atual/unified/validation.json) · [Hashes das capturas](../captures/atual/unified/SHA256SUMS.txt) · [Janela de validação do pacote](../downloads/v0.4.0-alpha.3/validation-inputs.json).

## Comandos utilizados

No ambiente em nuvem: JDK 8u472, Gradle 2.14.1, Minecraft 1.8.9 / Forge 11.15.1.2318, mapeamentos stable_22, Xvfb/Mesa por software. Os builds foram sequenciais; as sessões usam mundos descartáveis e limite de 30 FPS.

```bash
export ALIEN_TOOLS_DIR=/workspace/.alien-tools
for edition in unified legit full; do
  ./gradlew --no-daemon "-Pedition=$edition" clean build \
    smokeClasses skySmokeClasses combatSmokeClasses ultimateSmokeClasses
done

env -u DISPLAY ALIEN_EDITION=unified timeout 300 ./scripts/smoke-client.sh

# Display da sessão HUD/Full/partículas; encerrado após o jogo.
/workspace/.alien-tools/xvfb/root/usr/bin/Xvfb :96 \
  -screen 0 1280x720x24 +extension GLX +render -noreset -nolisten tcp -ac
# Em outro shell, com o display pronto:
DISPLAY=:96 LIBGL_ALWAYS_SOFTWARE=1 timeout 320 \
  ./gradlew --no-daemon -Pedition=unified runUltimateSmokeClient

# Display da sessão céu e combate; encerrado após as duas sessões.
/workspace/.alien-tools/xvfb/root/usr/bin/Xvfb :95 \
  -screen 0 1280x720x24 +extension GLX +render -noreset -nolisten tcp -ac
# Em outro shell, com o display pronto:
DISPLAY=:95 LIBGL_ALWAYS_SOFTWARE=1 timeout 300 \
  ./gradlew --no-daemon -Pedition=unified runSkySmokeClient
python3 scripts/package-unified.py --begin-validation
DISPLAY=:95 LIBGL_ALWAYS_SOFTWARE=1 timeout 300 \
  ./gradlew --no-daemon -Pedition=unified runCombatSmokeClient
python3 -m unittest discover -s scripts/tests -p 'test_package_unified.py' -v
GITHUB_SHA=18c84442ffab1cdbd84d70bec3d7b9e50c2c56ea \
  python3 scripts/package-unified.py
```

No CI, os wrappers versionados `ci-smoke-xorg.sh`, `ci-sky-smoke-xorg.sh`, `ci-combat-smoke-xorg.sh` e `ci-ultimate-smoke-xorg.sh` usam Xorg dummy/Mesa e verificam os resultados reais. Os screenshots e relatórios também ficam nos artifacts da execução Unified.

## Correções encontradas durante a validação

A primeira regressão geral após ampliar Armor Status falhou ao amostrar o céu: o novo painel cobria o pixel antigo. A captura confirmou o céu correto; a amostra foi movida para um ponto desobstruído, mantendo a comparação de cor (commit `bf036d9`). A suíte final passou os 161 checks. O teste de mira gradual passou a desligar explicitamente o novo Aimbot instantâneo na sua fixture (`f444557`). A ampliação do smoke de partículas teve um conflito de imports entre os dois WorldManager, corrigido antes da compilação final. Nenhuma falha foi convertida em aprovação sem rerodar.

## Limitações e recursos não implementados

Não foram testados gameplay Windows, GPU física, OptiFine, input humano ou servidor multiplayer externo. O check Windows do CI verifica apenas ZIP/JAR. Os alvos de treino são entidades client-side; não provam combate em rede. Fome, cura e bloco da ponte foram conferidos no servidor integrado real.

Anti Aim é giro real da câmera, sem fake angles/desync ou garantia contra acertos. Não há silent aim, bypass de anticheat, pathfinding de ponte até um destino, criação de blocos ou busca de comida/poções fora da hotbar. Os limites de partículas/renderização foram verificados; não há promessa medida de ganho de FPS. A opacidade do HUD afeta painéis/texto; ícones vanilla de itens mantêm seu renderizador original. Os novos módulos fazem parte do Unified; o gameplay das composições históricas foi preservado, sem exigir JARs separados para acessar Legit/Full no Unified.
