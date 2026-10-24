# Validação — Interactive Studio 0.6.0-alpha.1

Fonte `8bde0c5d3a97ebc30b3ff0c5eeaee34535cfab57`, branch `feature/interactive-studio-20261024`, PR #18 empilhado sobre #17, preservando #15/#16/#17. **Minecraft Java 1.8.9 / Forge 11.15.1.2318 / Java 8**. Nenhum merge na main.

| Validação final local | Resultado |
| --- | --- |
| clean build / JUnit / JAR — Unified | **175 testes**, zero falhas/erros/ignorados |
| clean build / JUnit / JAR — Legit | **175 testes**, zero falhas/erros/ignorados |
| clean build / JUnit / JAR — Full histórica | **175 testes**, zero falhas/erros/ignorados |
| Forge real — regressão geral | **PASS 161 checks** |
| Forge real — Sky Studio / importação / galeria | **PASS 37 checks** |
| Forge real — combate anterior | **PASS 24 checks** |
| Forge real — HUD/Full/cosméticos | **PASS 92 checks** |
| Forge real — Scene Studio | **PASS 70 checks** |
| Forge real — Interactive Studio | **PASS 125 checks** |
| Empacotamento / rejeição de evidência inválida | **11 testes Python aprovados** |
| JAR/ZIP experimental | **PASS** |

São **525 execuções JUnit** (175 por composição) e **509 verificações gráficas** em seis sessões Unified. Checks incluem capturas, controles, pixels, persistência e entidades; não são 509 comparações golden image nem benchmark. Evidência final: **24/10/2026, 21:36–21:41 UTC**.

[153 fotos originais](../captures/README.md) · [MP4 4min47s / GIF 2min59s](../captures/video-atual/README.md) · [Resultados/proveniência](../captures/atual/unified/validation.json) · [JAR/ZIP testado](../downloads/v0.6.0-alpha.1/).

## Verificações novas

- Sete telas novas verificadas dentro do Forge em **427×240**, com controles dentro dos limites e sem sobreposição.
- Menu renderiza geometria 3D/atlas vanilla sem abrir um mundo jogável. Controles reais selecionam cenário, hora, clima e câmera. Arrastar por **AWT Robot/X11** muda o ângulo real; scroll X11 navega os cards da galeria.
- Lista multiplayer vanilla mostra a cena real com Gaussian blur. Pixels confirmam que o fundo de terra não cobre o efeito; pixels do título verificam fontes reais, não blocos sólidos. Entrada local offline, seleção/Join habilitado e Refresh também verificados. Conexão a servidor externo não foi testada.
- Orbit abre a categoria real; Gallery pode ser escolhida para a tecla de menu sem remover os layouts anteriores.
- Presets são JSON reais do Unified. Handlers reais salvam/duplicam/renomeiam/exportam/copiam/importam/carregam; prévia calcula mudanças, captura local salva o framebuffer e perfil mostra estado em uso. JUnit verifica também preservação da associação por servidor ao renomear e rejeição de sobrescrita, traversal, JSON profundo e versão inválida.
- Mist, Bokeh e Radiance alteram mais de 100 amostras dos framebuffers reais em relação ao baseline. Radiance roda após a mão e antes do HUD; Bokeh usa profundidade, mantendo o HUD nítido. Não é uma medição óptica de qualidade.
- Name Tags mostram nome/vida reais; Target ESP usa a entidade viva real observada pelo Target HUD. **Uma parede colocada pelo servidor integrado bloqueia a linha de visão e ambos param de desenhar**, com captura e contadores verificados.
- Hand Movement executa o render real do item com transformações; **tecla C enviada via X11** atinge o hook real de FOV. Não é só uma animação de botão.
- Afterglow acompanha uma vaca realmente criada e morta no servidor integrado; a entidade chega ao client antes da morte. Verifica **uma única fonte**, emissão real/fade, deduplicação e limpeza em OFF/desconexão.
- Nas comparações dos estilos/paletas, a fixture fixa `skySpeed=0` para isolar pixels; o reset para 1 é verificado. A animação entre framebuffers é coberta pela suíte Scene anterior, não por uma comparação animada de cada um dos oito estilos novos.
- Oito estilos nomeados têm amostras de framebuffer distintas; paleta Rose muda pixels reais. Seleção, reset e OFF usam os handlers reais do editor de céu. Prévia usa o mesmo shader procedural dos céus.
- `GL_NO_ERROR` após as capturas; OFF interrompe trabalho dos shaders e limpa o histórico; horário do servidor permanece 6000, preparado pela fixture antes de ligar os efeitos.

Regressões anteriores de HUD, Full, capa/trilha/headwear, importação/cubemap, partículas, temas, perfis e performance foram reexecutadas. Fome, cura e blocos da ponte continuam conferidos no servidor integrado. O alvo jogador de treino é uma entidade client-side; isso está identificado e não equivale a multiplayer externo.

## Correções durante os testes

A primeira fixture assumia que `Minecraft.getIntegratedServer()` seria nulo no menu; o próprio Minecraft 1.8.9 pré-cria esse objeto no construtor. A verificação foi corrigida para ausência de mundo e de sessão singleplayer ativa, sem iniciar servidor para o cenário. Outra execução detectou colisão com perfis criados pela execução anterior; a fixture agora limpa apenas seus nomes conhecidos dentro do runtime descartável antes de começar. As falhas não foram publicadas como PASS.

A inspeção também encontrou o fundo de terra do GuiSlot cobrindo o blur e um estado de textura quebrando fontes nativas. O render da lista passou a ser transparente/recortado, com reconciliação de estado OpenGL; verificações de pixels visíveis e de glifos foram acrescentadas, mantendo entradas/seleção/Refresh nativos. A revisão de escala automática encontrou sobreposição em controles novos de cenário/céu e navegação; a correção ganhou sete verificações de layout e foi regravada antes da publicação. A inspeção visual motivou textura/vegetação/água no cenário e prévias reais dos shaders. O hook inicial de mão foi corrigido para renderizar dentro das matrizes de primeira pessoa, pois transformações aplicadas antes eram apagadas pelo render nativo. Tags usam fonte vanilla com teste de profundidade e preservam tags nativas acima do orçamento. A validação final usa somente a revisão citada.

Uma execução inicial do CI falhou antes de iniciar o Minecraft no passo Ultimate: `kill -0` recebia EPERM ao consultar o Xorg iniciado por sudo e encerrava a espera, apesar de o servidor gráfico estar ativo. A readiness agora usa `ps`, aguarda `xdpyinfo` e usa displays distintos por sessão. A falha está em [40134858611](https://github.com/AttackAlien/Alien-Client/actions/runs/40134858611); o client não foi alterado para contorná-la.

## Integridade

```text
JAR SHA-256: e4570c83a3429e4ff562fb2129e2a35c9f1bff66d2e70f95e28e563f30e9d830
Fontes/build SHA-256: cdf642e563b859586c51deda2e1ecf7ab2f554e2d02b4083cf51925e3d65e0ad
Classes/recursos SHA-256: a18f865b7f4a4b36cc4253069a2b89436cf5dceaa1f6fea5963174aea4778c24
JUnit SHA-256: 34939aef511c912ccb083e899713c1d24a42fbf6040d34ac8fea01976ce85bd0
```

Janela de validação aberta após os builds e antes do Combat smoke; Ultimate, Scene e Interactive executados depois com os mesmos inputs. O pacote contém o JAR verificado, shaders e créditos anteriores, sem classes dos mods de teste. A revisão de CI `a83bbe9494ce256ab1957807dd706aa192cb1bc8` altera somente seis scripts de Xorg, sem mudar fontes/shaders/classes/JUnit do client `8bde0c5`. A janela e os hashes confirmam os mesmos inputs. O commit de mídia posterior só publica documentos, capturas e o binário validado.

## Comandos executados

JDK **8u472**, Gradle **2.14.1**, mappings stable_22. Xvfb/Mesa por software em Linux, `DISPLAY=:94`, `LIBGL_ALWAYS_SOFTWARE=1`.

```bash
export ALIEN_TOOLS_DIR=/workspace/.alien-tools
for edition in unified legit full; do
  ./gradlew --no-daemon "-Pedition=$edition" clean build \
    smokeClasses skySmokeClasses combatSmokeClasses ultimateSmokeClasses \
    sceneSmokeClasses studioSmokeClasses
done
./gradlew --no-daemon -I /tmp/alien-video-size-06.gradle -Pedition=unified runSmokeClient
./gradlew --no-daemon -I /tmp/alien-video-size-06.gradle -Pedition=unified runSkySmokeClient
python3 scripts/package-unified.py --begin-validation
./gradlew --no-daemon -I /tmp/alien-video-size-06.gradle -Pedition=unified runCombatSmokeClient
./gradlew --no-daemon -I /tmp/alien-video-size-06.gradle -Pedition=unified runUltimateSmokeClient
./gradlew --no-daemon -I /tmp/alien-video-size-06.gradle -Pedition=unified runSceneSmokeClient
./gradlew --no-daemon -I /tmp/alien-video-size-06.gradle -Pedition=unified runStudioSmokeClient
python3 -m unittest discover -s scripts/tests -p test_package_unified.py -v
GITHUB_SHA=a83bbe9494ce256ab1957807dd706aa192cb1bc8 python3 scripts/package-unified.py
```

O init script temporário apenas acrescenta largura/altura de lançamento. Para desenvolvimento interativo: `./gradlew --no-daemon -Pedition=unified runClient` com o mesmo Java/display. No CI, `scripts/ci-studio-smoke-xorg.sh` executa a nova suíte em Xorg/Mesa.

[CI Unified / integridade Windows](https://github.com/AttackAlien/Alien-Client/actions/runs/40135317929) · [CI Forge Legit/Full](https://github.com/AttackAlien/Alien-Client/actions/runs/40135319617). Execuções da revisão de CI `a83bbe9494ce256ab1957807dd706aa192cb1bc8`, que contém exatamente o código de client `8bde0c5`. Estado e logs públicos nas execuções.

## Limites e itens ausentes

[Escopo exato de cada função](INTERACTIVE-STUDIO.md). Implementações próprias, sem port/cópia de Purxiy. Biblioteca é **offline**: Discover, conta, hosting, upload e compartilhamento por URL **não implementados**; copiar/exportar JSON funciona localmente. Cards ocultos do vídeo não têm funções adivinhadas. Preview de perfil compara switches, não todas as opções numéricas/layouts/cores.

Cenário do menu é um diorama, não um save jogável. Radiance é composição das bordas da cena inteira; Bokeh é aproximação de profundidade; Afterglow são partículas, não fragmentação do modelo. C é fixo para zoom. Os limites de Glass/X-Ray/Cute World anteriores permanecem. Hardware sem shader/framebuffer usa fallback com status real. Sem benchmark de FPS.

Não testados: GPU física, Windows gameplay, multiplayer externo, OptiFine, Iris/Sodium ou outros mods de render de mão/câmera. Windows CI verifica ZIP/JAR, não execução do jogo. Audio/OpenAL/Twitch e consultas de skin/autenticação podem emitir avisos no ambiente sem áudio/conta; os testes usam servidor integrado e a gravação é sem áudio. Nenhum login ou servidor externo é necessário para estes testes.

## Erros de log observados nas fixtures herdadas

Além dos avisos de áudio/skin, as sessões General e Scene registraram respectivamente **81 e 75 exceções não fatais** em `NetHandlerPlayClient.handleChunkData`, ao processar `S21PacketChunkData` já enfileirados **depois** do descarregamento das dimensões na saída automatizada. As verificações posteriores de menu/OFF/limpeza passaram; isso não significa logs sem erros. Esse comportamento de teardown das fixtures anteriores permanece como limitação observada. O novo Studio fecha a conexão antes de descarregar o mundo e não registrou esse erro. [Resumo extraído dos logs reais](../captures/atual/unified/known-log-errors.txt).
