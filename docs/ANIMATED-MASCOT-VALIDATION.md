# Validação — Mascot Studio / Unified 0.11.0-alpha.1

[PR #28](https://github.com/AttackAlien/Alien-Client/pull/28), branch `feature/animated-mascot-20261104`, baseada na main integrada. Sem merge ou novo release desta implementação; o release público 0.10 foi preservado. [Guia](ANIMATED-MASCOT.md).

## Resultados de 04/11/2026

**237 JUnit**, sem falhas/erros/ignorados; **825 checks gráficos** em seis sessões Forge 1.8.9; **12 testes de empacotamento** incluindo rejeição de evidência obsoleta.

| Sessão | Resultado |
| --- | --- |
| general | PASS 161 checks |
| sky | PASS 37 checks |
| combat | PASS 24 checks |
| ultimate | PASS 390 checks |
| scene | PASS 71 checks |
| studio | PASS 142 checks |

Legit/Full: 237 JUnit + 155 checks gráficos por composição. Sky Studio: 237 + 37 por composição. Launcher: 31 testes Linux, 31 Windows, 46 checks da janela nativa, seis capturas e instalação isolada Java 8/Forge/JAR 0.11 aprovados. ZIPs com CRC completo e EXE AMD64 verificados.

A rodada local direcionada passou **65 checks** do mascote. Os checks incluem estados, controles e capturas; não representam centenas de golden images ou um benchmark. [Recibos originais, XMLs e metadados](mascot-results/2026-11-04/).

### Estado do workflow do launcher

A API autenticada e a API pública ainda retornam `status: queued` e `conclusion: null` para o workflow 41877291508, fonte `abcbf57908af259fbf89e67ae8f471c0cc47db12`. O workflow completo **não é declarado concluído**. Entretanto, o GitHub já disponibiliza seus dois artifacts, produzidos entre 17:25 e 17:29 UTC, com os relatórios originais: 31 testes Linux e 31 Windows aprovados, 46 checks da janela nativa, seis PNGs e `INSTALL_PASS` para Java 8/Forge/JAR 0.11. Os metadados dos artifacts apontam para a mesma execução e revisão.

Esses recibos, os hashes das seis imagens, o CRC integral do ZIP, a arquitetura AMD64 do EXE e a identidade exata do JAR/payload foram verificados independentemente. O pacote é entregue como candidato do PR, sem merge ou novo release. [Resposta do workflow](mascot-results/2026-11-04/launcher-ci-run.json) · [Consulta pública](mascot-results/2026-11-04/launcher-public-run.json) · [Metadados dos artifacts](mascot-results/2026-11-04/launcher-artifacts.json) · [Resumo das verificações](mascot-results/2026-11-04/launcher-summary.json).

## Exercitado no jogo real

A navegação Pet abriu o editor sem remover os estúdios existentes. O renderer desenhou o mesmo mascote na prévia ampliada e no mundo; as paletas Alien/Mint/Solar chegaram aos materiais reais. O seletor HEX foi editado pelo handler de teclado e o valor foi persistido. Perfil Unified e slots de looks restauraram configurações independentes.

Movimento nativo X11 elevou a resposta de movimento e deslocou o dono real. Ao parar, o seguidor estabilizou perto dele. Floss real ativou o aceno; desligar React to emotes liberou a reação enquanto o emote continuava. Shoulder colocou o mascote à altura do ombro; Orbit moveu sua posição X/Z enquanto o dono continuava parado.

Animation OFF manteve uma figura estática visível. Um teleporte real do servidor reposicionou o seguidor perto do dono. Aguardar a inatividade colocou o mascote em repouso; movimento nativo o despertou. Rolagem X11 expôs o último slider no layout compacto. Orçamento zero e Mascot OFF limparam estados e geometria; restaurar orçamento ou carregar perfil recriou o companheiro. Os passes e editores terminaram sem erros OpenGL e com escopos de emotes equilibrados.

Os testes JUnit cobrem migração com opt-in desligado, normalização de importações corrompidas, presets, cópias independentes em looks/perfis, persistência junto de emotes/auras, acompanhamento amortecido, teleporte sem interpolação longa, modos distintos, repouso/despertar, gatilho de emotes, independência da quantidade de frames e da subdivisão do tick, entradas inválidas, animação desligada, piscada/aceno e geometria finita/limitada.

A geometria original usa partes arredondadas compartilhadas e o cache limitado de meshes existente. O renderer suporta até quatro estados locais e prioriza o dono ao reduzir o orçamento. O mascote é visual; não cria entidades, pacotes, inventário ou navegação física no servidor.

## Vídeo e galeria atual

**[Assistir / baixar MP4](../captures/video-atual/Alien-Client-0.11-customizacoes-full.mp4)** — 123.69s, 960×540, H.264/YUV420p, **29.97 FPS codificados** (`30000/1001`; captura solicitada a 30 FPS), sem áudio. Mediana de renderização **57.9 FPS**, P95 **28.6 ms/frame**, nesta tomada Linux/Mesa por software.

MP4 original contínuo de ffmpeg x11grab, copiado byte a byte, sem reedição, aceleração, sequência de fotos ou interpolação. O validador foi ajustado para aceitar e registrar a taxa nativa 29,97/30; os argumentos de captura e os frames não foram alterados. [Recibo do ajuste](mascot-results/2026-11-04/video/recording-validator.json). Não é uma promessa de desempenho em outra máquina.

Quatorze capítulos mostram as customizações anteriores e o mascote em Follow, Shoulder e Orbit. O trecho do mascote começa por volta de **01:45** e confirma respostas a movimento nativo e emote real. Os módulos Full continuam demonstrados com alinhamento da câmera, alimentação, cura por poção e colocação de blocos em Survival. As legendas identificam o fixture automatizado em mundo local; ele não é distribuído no JAR.

**336 PNGs novos do jogo e seis do launcher** substituem as imagens anteriores das pastas atuais. Uma captura é da janela X11; as demais são originais do framebuffer, copiadas sem alteração do CI. Os cinco GIFs usam somente os PNGs novos. Mascote, emotes, auras e trilhas usam intervalos registrados; capa usa 100 ms/frame na conversão. A imagem de prévia do filme é um frame extraído aos 114s. [Manifesto](../captures/atual/unified/validation.json) · [SHA-256 do MP4](../captures/video-atual/SHA256SUMS.txt).

## Fontes e pacotes

Client: `5e5621e73fbe05f3a1d948a5e5ee41f44b0067aa`, [CI 41874722323](https://github.com/AttackAlien/Alien-Client/actions/runs/41874722323), JAR SHA-256 `8a4e253baab54a04f9065d0d248f3d9d937cf5cf64a1aa81929e92e978193d43`.

Vídeo: `2eef9a5227cff07830300d8d05a6d0b01aacc4a0`. Os objetos Git de src/main, src/unified e src/showcase coincidem com os do CI do client. A diferença posterior afeta apenas validação e o teardown do teste.

Launcher 0.3.0-alpha.8: `abcbf57908af259fbf89e67ae8f471c0cc47db12`, [CI 41877291508](https://github.com/AttackAlien/Alien-Client/actions/runs/41877291508), ZIP SHA-256 `277675c6043c8f989d02615b896a602b4a89d41b958e2e053cb150c420bb3f45`, 70,813,134 bytes. Mantém o layout/emblema aprovados e inclui exatamente o JAR aprovado.

[Client candidato](../downloads/v0.11.0-alpha.1/README.md) · [Launcher candidato](../downloads/launcher/v0.3.0-alpha.8/README.md). A publicação de mídia e downloads usa `[skip ci]` e conserva os arquivos de execução validados. Após a captura, mudaram o validador de taxa do gravador e o desligamento do mundo no fixture Studio; os fontes de produção e do filme continuam idênticos. A sintaxe do gravador e a leitura dos metadados reais passaram localmente. [Objetos Git comparados](mascot-results/2026-11-04/validated-source-trees.json).

## Comandos e limites

```bash
export ALIEN_TOOLS_DIR=/workspace/.alien-tools
export GRADLE_USER_HOME=/workspace/Alien-Client/.gradle-user
./gradlew --offline --no-daemon -Pedition=unified test showcaseClasses
DISPLAY=:117 LIBGL_ALWAYS_SOFTWARE=1 LP_NUM_THREADS=2 \
  ./gradlew --offline --no-daemon -Pedition=unified -PpetOnly runUltimateSmokeClient
ALIEN_SHOWCASE_DISPLAY=:118 bash scripts/record-showcase.sh
```

Xorg :117 usa scripts/ci-combat-xorg.conf com `-noreset -nolisten tcp -ac`. O gravador iniciou seu próprio display :118, aguardou o cenário real aquecer e capturou a janela.

Minecraft 1.8.9 / Forge 11.15.1.2318 / Java 8u472. Sem validação de GPU física, gameplay em Windows pessoal, autenticação/licença Microsoft, multiplayer externo ou OptiFine. Emotes, auras e mascotes são locais; não há sincronização de danças/companheiros pela rede.

O ambiente não tem áudio funcional. General/Scene ainda registram tarefas de chunks após descarregar mundos: 81 e 75 ocorrências de pilha nesta rodada; não são contagens de quedas do jogo. As seis sessões terminaram com PASS. [Ocorrências selecionadas](../captures/atual/unified/known-log-errors.txt); logs completos nos artifacts.

Nos ensaios preliminares, o limite do braço detectou sobreposição de caminhada/aceno e foi corrigido com blend; o teste de órbita passou a medir deslocamento planar X/Z, pois um único eixo pode momentaneamente não variar. O validador de vídeo inicialmente exigia exatamente 30/1; agora aceita a taxa nativa registrada, sem alterar a gravação. O primeiro CI travou ao encerrar o mundo no teste Studio: a conexão era fechada antes de loadWorld(null), permitindo ao servidor parar antes de processar a tarefa de shutdown. O fixture passou a deixar loadWorld realizar a sequência completa, mantendo todas as verificações. [Diagnóstico preservado](mascot-results/2026-11-04/first-ci-studio-timeout.json).
