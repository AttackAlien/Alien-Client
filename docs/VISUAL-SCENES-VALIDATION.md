# Validação — Scene Studio 0.5.0-alpha.1

Fonte `3a8ee39413c8309168a13ee3a6295e0b4b68e3b6`, branch `feature/visual-scenes-20261024`, PR #17 empilhado sobre #16, preservando os PRs #15/#16 e o Unified. Minecraft continua **1.8.9 / Forge 11.15.1.2318 / Java 8**. Nenhum merge na main.

| Verificação final local | Resultado |
| --- | --- |
| clean build / JUnit / JAR — Unified | **165 testes**, zero falhas/erros/ignorados |
| clean build / JUnit / JAR — Legit | **165 testes**, zero falhas/erros/ignorados |
| clean build / JUnit / JAR — Full histórica | **165 testes**, zero falhas/erros/ignorados |
| Forge real — regressão geral | **PASS 161 checks** |
| Forge real — Sky Studio | **PASS 37 checks** |
| Forge real — combate anterior | **PASS 24 checks** |
| Forge real — HUD/Full/cosméticos | **PASS 92 checks** |
| Forge real — nove funcionalidades Scene Studio | **PASS 70 checks** |
| Empacotamento/rejeição de evidência inválida ou antiga | **11 testes Python aprovados** |
| Pacote JAR/ZIP experimental | **PASS** |

São **495 execuções JUnit** (165 por composição) e **384 verificações gráficas**, incluindo controles, estados, pixels/framebuffers e persistência. Os checks incluem as capturas; não são 384 comparações golden image ou benchmark. As cinco sessões gráficas são da Unified.

## Evidência e o que foi testado

As cinco sessões finais foram gravadas em 24/10/2026, 16:53–16:57 UTC. [117 PNGs atuais](../captures/README.md), [vídeo 3min48s / GIF 2min15s](../captures/video-atual/README.md), [resultados/proveniência](../captures/atual/unified/validation.json), [candidato de instalação](../downloads/v0.5.0-alpha.1/).

O Scene smoke confirma:

- Compilação/vínculo GLSL 1.20 real, onda após salto/pouso real e composição do framebuffer com profundidade.
- Rosto original com piscar e esqueleto acompanhando as poses reais vanilla.
- Contadores separados confirmam material Glass no jogador, espada segurada e item solto sincronizado pelo servidor integrado. O item foi realmente criado em chunk carregado, sem substituir a verificação por uma imagem.
- Chroma usa amostras reais de movimento via teclado X11 e mantém o histórico limitado; sair do mundo e OFF limpam o estado.
- Oito amostras de cor diferentes nos céus renderizados e diferenças em mais de 100 pixels entre duas capturas consecutivas do mesmo céu com a câmera parada, comprovando animação.
- Botões reais de Shockwave/Cape/Trail/Cute/materiais/Sky/master e perfis unificados foram acionados pelos handlers das telas. Capa/trilha e todos os campos novos persistem.
- `GL_NO_ERROR` após cada captura e restauração do render nativo ao desligar/desconectar. O horário real do servidor continua em 6000, definido apenas na fixture antes dos efeitos.

Regressões de HUD, Full, armadura/itens, capas, looks, temas, perfis, performance e céu anteriores continuam aprovadas. Fome/cura/bloco da ponte são conferidos no servidor integrado real. Alvos de treino são entidades client-side; input X11 é automatizado, não humano.

Na primeira verificação separada de item solto, o teste falhou porque a fixture o criava antes de o chunk estar carregado após teleporte. A fixture passou a carregar o chunk, confirmar a criação no servidor e depois verificar os três contadores de render; a falha não foi ignorada. A inspeção dos framebuffers também corrigiu a grade em superfícies horizontais e a densidade de estrelas antes da revisão final.

## Integridade do artefato

```text
JAR SHA-256: 28617f6d0bd352eec48c007031155abe0382d930cec5c9d278f894f39d2bfa38
Fontes/build SHA-256: 956269574d6007baf381c607df3313a7a0005dcd4966ac1a11d7b867bfd5bd3a
Classes/recursos SHA-256: 286a9112666763d806605430995bcd50b6e35736fbd7b0a1290948febfba6620
JUnit SHA-256: 08e0a1e189eb5cf191cd9063cd87547af9a61d03f8de8e5edcf88f1457866b32
```

A janela de validação foi aberta após compilar/testar e antes da sessão Combat; as sessões Ultimate e Scene ocorreram depois, sem alterar esses inputs. O pacote contém o mesmo JAR verificado, sem classes dos mods de teste, e mantém o crédito do tecido CC0. Fonte/shaders, classes/recursos e JUnit permaneceram vinculados pela mesma janela. O commit posterior publica apenas documentos, mídias e binário validado.

## Comandos

JDK 8u472, Gradle 2.14.1, stable_22, Xvfb/Mesa em Linux. Builds sequenciais:

```bash
export ALIEN_TOOLS_DIR=/workspace/.alien-tools
for edition in unified legit full; do
  ./gradlew --no-daemon "-Pedition=$edition" clean build \
    smokeClasses skySmokeClasses combatSmokeClasses ultimateSmokeClasses sceneSmokeClasses
done
# DISPLAY=:94, Xvfb 1280x720x24, LIBGL_ALWAYS_SOFTWARE=1; FFmpeg grava cada sessão.
./gradlew --no-daemon -I /tmp/alien-video-size.gradle -Pedition=unified runSmokeClient
./gradlew --no-daemon -I /tmp/alien-video-size.gradle -Pedition=unified runSkySmokeClient
python3 scripts/package-unified.py --begin-validation
./gradlew --no-daemon -I /tmp/alien-video-size.gradle -Pedition=unified runCombatSmokeClient
./gradlew --no-daemon -I /tmp/alien-video-size.gradle -Pedition=unified runUltimateSmokeClient
./gradlew --no-daemon -I /tmp/alien-video-size.gradle -Pedition=unified runSceneSmokeClient
python3 -m unittest discover -s scripts/tests -p test_package_unified.py -v
GITHUB_SHA=3a8ee39413c8309168a13ee3a6295e0b4b68e3b6 python3 scripts/package-unified.py
```

O init script temporário só define a resolução inicial; o código do jogo não foi modificado para produzir vídeo. No CI, `scripts/ci-scene-smoke-xorg.sh` executa a nova suíte com Xorg/Mesa e publica PNGs/relatório no artifact, junto das quatro suítes anteriores.

[CI Unified/Windows integridade](https://github.com/AttackAlien/Alien-Client/actions/runs/40113574688) · [CI Forge Legit/Full](https://github.com/AttackAlien/Alien-Client/actions/runs/40113576641). Ambas as execuções aprovaram os quatro checks da revisão de código: Unified Java 8, Windows ZIP/JAR, Forge Legit e Forge Full. Disparos manuais por ser um PR empilhado; o commit posterior só publica a evidência e o binário já validado.

## Limites reais

São versões originais Alien dos efeitos identificados, com [escopo e diferenças documentados](VISUAL-SCENES.md), não um port do produto Purxiy. Não há assets, shader ou vídeo proprietário incluído. Cute World altera rostos de jogadores; X-Ray é esqueleto decorativo e preserva oclusão do terreno; Glass trata jogadores e itens no mundo/segurados em terceira pessoa, mantendo mãos/ícones em primeira pessoa/inventário vanilla. A armadura vanilla é ocultada visualmente nos modos Skeleton/Glass e reaparece em Natural.

Os céus são oito presets procedurais originais, não imagens copiadas. Shockwave/Chroma não alteram blocos, chunks, horário ou regras. Há limites de distância/histórico/orçamento. É necessário framebuffer/shaders; hardware incompatível mostra o motivo e continua com render nativo. Windows gameplay, GPU física, OptiFine, Iris/Sodium e multiplayer externo não foram testados. O check Windows é de integridade de ZIP/JAR. Sem benchmark de FPS ou promessa de PBR/ray tracing. Os limites dos módulos Full anteriores permanecem.
