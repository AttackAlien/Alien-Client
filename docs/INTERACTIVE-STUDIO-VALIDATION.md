# Validação — Interactive Studio 0.6.0-alpha.2

Fonte `32829c561b8dc53719a71b7a6621c58cb5a2e8f5`, branch `feature/interactive-studio-20261024`, PR #18 empilhado sobre #17, preservando #15/#16/#17. **Minecraft Java 1.8.9 / Forge 11.15.1.2318 / Java 8**. Nenhum merge na main.

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
| Forge real — Interactive Studio | **PASS 142 checks** |
| Empacotamento / rejeição de evidência inválida | **11 testes Python aprovados** |
| JAR/ZIP experimental | **PASS** |

São **525 execuções JUnit** (175 por composição) e **526 verificações gráficas** em seis sessões Unified. Checks incluem capturas, controles, pixels, persistência e entidades; não são 526 comparações golden image nem benchmark. Evidência final: **24/10/2026, 21:59–22:05 UTC**.

[154 fotos originais](https://github.com/AttackAlien/Alien-Client/blob/8bf23165744e0fc630689c1de76ca0b6316a191c/captures/README.md) · [MP4 4min48s / GIF 2min59s](https://github.com/AttackAlien/Alien-Client/blob/8bf23165744e0fc630689c1de76ca0b6316a191c/captures/video-atual/README.md) · [Resultados/proveniência](https://github.com/AttackAlien/Alien-Client/blob/8bf23165744e0fc630689c1de76ca0b6316a191c/captures/atual/unified/validation.json) · [JAR/ZIP testado](../downloads/v0.6.0-alpha.2/).

## Verificações novas

- Apresentação real da janela verificada por captura AWT Robot/X11, comparada ao framebuffer em todas as telas do Studio. Diferenças entre frames consecutivos são toleradas; tela branca não passa. Foto 127 é a captura direta da janela.

- Sete telas novas verificadas dentro do Forge em **427×240**, com controles dentro dos limites e sem sobreposição.
- Menu renderiza geometria 3D/atlas vanilla sem abrir um mundo jogável. Controles reais selecionam cenário, hora, clima e câmera. Arrastar por **AWT Robot/X11** muda o ângulo real; scroll X11 navega os cards da galeria.
- Lista multiplayer vanilla mostra a cena real com Gaussian blur. Pixels confirmam que o fundo de terra não cobre o efeito; pixels do título verificam fontes reais, não blocos sólidos. Entrada local offline, seleção/Join habilitado e Refresh também verificados. Conexão a servidor externo não foi testada.
- Orbit abre a categoria real; Gallery pode ser escolhida para a tecla de menu sem remover os layouts anteriores.
- Presets são JSON reais do Unified. Handlers reais salvam/duplicam/renomeiam/exportam/copiam/importam/carregam; prévia calcula mudanças, captura local salva o framebuffer e perfil mostra estado em uso. JUnit verifica também preservação da associação por servidor ao renomear e rejeição de sobrescrita, traversal, JSON profundo e versão inválida.
- Mist, Bokeh e Radiance alteram mais de 100 amostras dos framebuffers reais em relação ao baseline. Radiance roda após a mão e antes do HUD; Bokeh usa profundidade, mantendo o HUD nítido. Não é uma medição óptica de qualidade.
- Name Tags mostram nome/vida reais; Target ESP usa a entidade viva real observada pelo Target HUD. **Uma parede colocada pelo servidor integrado bloqueia a linha de visão e ambos param de desenhar**, com captura e contadores verificados.
- Hand Movement executa o render real do item com transformações; **tecla C enviada via X11** atinge o hook real de FOV. Não é só uma animação de botão.
- Afterglow acompanha uma vaca realmente criada e morta no servidor integrado; a entidade chega ao client antes da morte. Verifica **uma única fonte**, emissão real/fade, deduplicação e limpeza em OFF/desconexão.
- Oito estilos nomeados têm amostras de framebuffer distintas; paleta Rose muda pixels reais. Seleção, reset e OFF usam os handlers reais do editor de céu. Prévia usa o mesmo shader procedural dos céus.
- `GL_NO_ERROR` após as capturas; OFF interrompe trabalho dos shaders e limpa o histórico; horário do servidor permanece 6000, preparado pela fixture antes de ligar os efeitos.

Regressões anteriores de HUD, Full, capa/trilha/headwear, importação/cubemap, partículas, temas, perfis e performance foram reexecutadas. Fome, cura e blocos da ponte continuam conferidos no servidor integrado. O alvo jogador de treino é uma entidade client-side; isso está identificado e não equivale a multiplayer externo.

## Correções durante os testes

A primeira fixture assumia que `Minecraft.getIntegratedServer()` seria nulo no menu; o próprio Minecraft 1.8.9 pré-cria esse objeto no construtor. A verificação foi corrigida para ausência de mundo e de sessão singleplayer ativa, sem iniciar servidor para o cenário. Outra execução detectou colisão com perfis criados pela execução anterior; a fixture agora limpa apenas seus nomes conhecidos dentro do runtime descartável antes de começar. As falhas não foram publicadas como PASS.

A inspeção também encontrou o fundo de terra do GuiSlot cobrindo o blur e um estado de textura quebrando fontes nativas. O render da lista passou a ser transparente/recortado, com reconciliação de estado OpenGL; verificações de pixels visíveis e de glifos foram acrescentadas, mantendo entradas/seleção/Refresh nativos. A revisão de escala automática encontrou sobreposição em controles novos de cenário/céu e navegação; a correção ganhou sete verificações de layout e foi regravada antes da publicação. A inspeção visual motivou textura/vegetação/água no cenário e prévias reais dos shaders. O hook inicial de mão foi corrigido para renderizar dentro das matrizes de primeira pessoa, pois transformações aplicadas antes eram apagadas pelo render nativo. Tags usam fonte vanilla com teste de profundidade e preservam tags nativas acima do orçamento. A inspeção do vídeo também detectou um erro de estado de textura: alguns menus tinham PNG correto no framebuffer, mas janela branca. A alpha.2 reconcilia o cache OpenGL antes da apresentação; agora há comparação com a janela X11 em todas as telas capturadas pelo Studio e um PNG direto dessa janela. A validação final usa somente a revisão citada.

Uma execução inicial do CI falhou antes de iniciar o Minecraft no passo Ultimate: `kill -0` recebia EPERM ao consultar o Xorg iniciado por sudo e encerrava a espera, apesar de o servidor gráfico estar ativo. A readiness agora usa `ps`, aguarda `xdpyinfo` e usa displays distintos por sessão. A falha está em [40134858611](https://github.com/AttackAlien/Alien-Client/actions/runs/40134858611); o client não foi alterado para contorná-la.

## Integridade

```text
JAR SHA-256: 3f5a60e0e3d765ef2fd34b04c0b602a6c06f8fd7c422caa133746dd8b5e70550
Fontes/build SHA-256: c04537c831e6f8444e901a9f66acf0713e20ca44f3da4f96e3349e391eb52b83
Classes/recursos SHA-256: 5e054cf8531d6af528e268a704e3dc3f66d2cc38f67e2744d7378b05633e6b2e
JUnit SHA-256: 64ec94fa3d1227cc3c7824910f5144d09acfe87df0542e8635c686e729b04cd5
```

Janela de validação aberta após os builds e antes do Combat smoke; Ultimate, Scene e Interactive executados depois com os mesmos inputs. O pacote contém o JAR verificado, shaders e créditos anteriores, sem classes dos mods de teste. O snapshot final `32829c561b8dc53719a71b7a6621c58cb5a2e8f5` inclui a correção de apresentação da janela e os scripts Xorg já corrigidos. A janela e os hashes confirmam os mesmos inputs. O commit de mídia posterior só publica documentos, capturas e o binário validado.

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
GITHUB_SHA=32829c561b8dc53719a71b7a6621c58cb5a2e8f5 python3 scripts/package-unified.py
```

O init script temporário apenas acrescenta largura/altura de lançamento. Para desenvolvimento interativo: `./gradlew --no-daemon -Pedition=unified runClient` com o mesmo Java/display. No CI, `scripts/ci-studio-smoke-xorg.sh` executa a nova suíte em Xorg/Mesa.

[CI Unified / integridade Windows](https://github.com/AttackAlien/Alien-Client/actions/runs/40136496598) · [CI Forge Legit/Full](https://github.com/AttackAlien/Alien-Client/actions/runs/40136498074). Execuções do snapshot final `32829c561b8dc53719a71b7a6621c58cb5a2e8f5`. **Ambas concluídas com sucesso**: Unified Java 8, integridade ZIP/JAR Windows, Forge Legit e Forge Full. Estado e logs públicos nas execuções.

## Limites e itens ausentes

[Escopo exato de cada função](INTERACTIVE-STUDIO.md). Implementações próprias, sem port/cópia de Purxiy. Biblioteca é **offline**: Discover, conta, hosting, upload e compartilhamento por URL **não implementados**; copiar/exportar JSON funciona localmente. Cards ocultos do vídeo não têm funções adivinhadas. Preview de perfil compara switches, não todas as opções numéricas/layouts/cores.

Cenário do menu é um diorama, não um save jogável. Radiance é composição das bordas da cena inteira; Bokeh é aproximação de profundidade; Afterglow são partículas, não fragmentação do modelo. C é fixo para zoom. Os limites de Glass/X-Ray/Cute World anteriores permanecem. Hardware sem shader/framebuffer usa fallback com status real. Sem benchmark de FPS.

Não testados: GPU física, Windows gameplay, multiplayer externo, OptiFine, Iris/Sodium ou outros mods de render de mão/câmera. Windows CI verifica ZIP/JAR, não execução do jogo. Audio/OpenAL/Twitch e consultas de skin/autenticação podem emitir avisos no ambiente sem áudio/conta; os testes usam servidor integrado e a gravação é sem áudio. Nenhum login ou servidor externo é necessário para estes testes.

## Limitação observada nos logs das fixtures

Na saída automatizada, General e Scene registraram respectivamente **72 e 77 exceções não fatais** de `NetHandlerPlayClient.handleChunkData`, ao processar `S21PacketChunkData` já enfileirados depois de descarregar as dimensões. As verificações de menu/OFF/limpeza passaram; isso não equivale a logs sem erros. O Studio fecha a conexão antes de descarregar e não registrou esse erro. [Contagem extraída dos logs reais](../captures/atual/unified/known-log-errors.txt).
