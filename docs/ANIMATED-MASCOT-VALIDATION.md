# Validação — dois modelos prontos / Unified 0.11.0-alpha.1

[PR #28](https://github.com/AttackAlien/Alien-Client/pull/28), branch `feature/animated-mascot-20261104`. Sem merge ou novo release; o release público 0.10 foi preservado. [Como usar](ANIMATED-MASCOT.md).

## Resultados de 05/11/2026

**253 JUnit, 872 checks gráficos e 12 testes de empacotamento aprovados**, sem testes JUnit falhando, ignorados ou com erro.

| Sessão Forge | Resultado |
| --- | --- |
| general | PASS 161 checks |
| sky | PASS 37 checks |
| combat | PASS 24 checks |
| ultimate | PASS 437 checks |
| scene | PASS 71 checks |
| studio | PASS 142 checks |

Legit/Full: 253 JUnit + 155 checks gráficos por composição. Sky Studio: 253 + 37 por composição. Rodada local direcionada: **112 checks**. Launcher: **31 testes Linux + 31 Windows, 46 checks da janela nativa, seis PNGs e instalação Java 8/Forge/JAR aprovados**, workflow concluído com sucesso. CRC do ZIP, arquitetura AMD64 e payload exato conferidos.

Os checks gráficos incluem estados, controles e capturas; não são centenas de golden images ou um benchmark. [Recibos originais, XMLs, TRX, metadados e etapa dos 12 testes](mascot-results/2026-11-05/).

## Modelos e comportamento exercitados

| Modelo pronto | Autor/licença | Dados originais | Comportamento ligado ao jogo |
| --- | --- | --- | --- |
| [Alien](https://poly.pizza/m/HYUUkdugoP) | Quaternius / CC0 | 7.642 vértices, 3.774 triângulos, 45 juntas, 14 clips | Idle, Walk, Clapping, Sitting, com transições graduais |
| [Tung Tung Tung Sahur](https://poly.pizza/m/Q6SvoDhhwA) | CalinnHotCake / CC BY 3.0 | 1.792 vértices, 872 triângulos, seis materiais, sem rig/clips | Flutuação, inclinação e balanço do conjunto; Follow/Shoulder/Orbit |

Os GLBs são os arquivos baixados dos modelos, preservados com hashes, licenças e atribuição em [third_party](../third_party/). A conversão normaliza coordenadas e escala; não reconstrói personagens com primitivas. O alien conserva os 14 clips nos dados, dos quais quatro estão ligados ao comportamento. O Tung não tem dança articulada, animação facial nativa ou áudio. Os materiais dos olhos/reflexos permanecem protegidos das paletas; o preset Original restaura as cores base. A iluminação do Forge não reproduz o renderizador PBR do site.

O seletor Model foi acionado no editor real: o renderer alternou entre os dois meshes e voltou ao alien. Movimento nativo X11 ativou acompanhamento, inclinação e os canais reais de animação; Floss do jogador ativou a reação. Os modos de ombro e órbita alteraram as posições reais. Animação desligada manteve os modelos visíveis; orçamento zero removeu estados/geometria e restaurar orçamento recriou o modelo escolhido.

Perfis Unified e looks restauraram modelo, cores e modo, mantendo slots independentes. O editor compacto continuou rolando até o último slider após adicionar o seletor. Teleporte real, inatividade, movimento para despertar, HSV/HEX e master de cosméticos foram exercitados. Os passes terminaram com escopos de emotes equilibrados e sem erros OpenGL.

JUnit cobre hashes do GLB/recurso, dados e clips reais, skinning finito, normais, deformações distintas, continuidade, independência da quantidade de frames, rejeição de dados corrompidos, migração de perfis sem model, cópias/persistência e orientação do rosto do Tung. A conferência visual pegou a orientação inicial virada para trás; ela foi corrigida e recebeu um teste de geometria. O primeiro ensaio do conversor detectou índices de juntas gravados com largura incompatível, corrigidos antes da rodada aprovada.

O renderer reutiliza malha, clips, matrizes e buffers e envia um batch por mascote. O limite continua sendo quatro estados visuais, com prioridade do dono e orçamento de cosméticos. Troca de mundo/perfil, morte, invisibilidade, distância e desligamento limpam os estados. Não são criadas entidades nem enviados comandos pelo sistema de mascotes.

## Capturas e vídeo novos

**368 PNGs do jogo, seis do launcher e seis GIFs** substituem a galeria atual. Uma imagem do jogo é da janela X11; as demais são originais do framebuffer, copiadas byte a byte do CI aprovado. As seis do launcher são capturas da janela Windows real. Os GIFs usam somente os PNGs novos, sem quadros sintéticos; capa usa 100 ms/frame, os demais usam os horários registrados. [Manifesto/hashes](https://github.com/AttackAlien/Alien-Client/blob/372c4b98ffe37d1d81032f154e8ebcb952069a39/captures/atual/unified/validation.json).

**[MP4 contínuo original](https://github.com/AttackAlien/Alien-Client/blob/372c4b98ffe37d1d81032f154e8ebcb952069a39/captures/video-atual/Alien-Client-0.11-customizacoes-full.mp4)**: 141.73s, 960×540, H.264/YUV420p, **29.986 FPS médios codificados** (`31875/1063`; solicitados 30 FPS), sem áudio. Mediana do jogo **57.0 FPS**, P95 **30.1 ms/frame**, nesta tomada Linux/Mesa por software. Quinze capítulos; alien por volta de 01:45 e Tung por volta de 02:03.

Sem reedição, aceleração, interpolação posterior ou montagem com fotos. O arquivo é a saída original do FFmpeg x11grab. Os módulos Full continuam demonstrados com câmera, alimentação, cura por poção e blocos colocados em Survival; as legendas identificam o fixture automatizado em mundo local. Ele não é distribuído no JAR. A prévia do vídeo é um frame real extraído aos 132s. [Recibos do filme](mascot-results/2026-11-05/video/).

## Revisões e pacotes

Client `1568db3f9cd76edc4c48fb3cc73961ca5a776f1f`, [CI 42027857579](https://github.com/AttackAlien/Alien-Client/actions/runs/42027857579), JAR SHA-256 `0b3014421e5266088bba31d64fd04294713bcd1b14f2ba79e9de7e9c7eace41a`. Filme capturado na mesma revisão. Launcher 0.3.0-alpha.9 `df953f1ae927bdf03051e1f58965dcae272d3a68`, [CI 42030955347](https://github.com/AttackAlien/Alien-Client/actions/runs/42030955347), ZIP SHA-256 `ce5cf621ae30404cfe8aa3fd87541675b061b7cd7d10e3985f4950747029a1a6`, 72,282,650 bytes.

[Client candidato](../downloads/v0.11.0-alpha.1/README.md) · [Launcher candidato](../downloads/launcher/v0.3.0-alpha.9/README.md). A publicação de mídia/recibos usa `[skip ci]` e conserva os arquivos de execução validados. [Objetos Git comparados](mascot-results/2026-11-05/validated-source-trees.json).

A pendência de metadados do antigo launcher alpha.8 foi resolvida: a API agora confirma completed/success. O registro de 04/11 continua preservado; [consulta de 05/11](mascot-results/2026-11-05/prior-launcher-status-resolved.json). O launcher alpha.9 desta entrega também concluiu com sucesso.

## Comandos e limites

```bash
export ALIEN_TOOLS_DIR=/workspace/.alien-tools
export GRADLE_USER_HOME=/workspace/Alien-Client/.gradle-user
./gradlew --offline --no-daemon -Pedition=unified test ultimateSmokeClasses showcaseClasses
DISPLAY=:120 LIBGL_ALWAYS_SOFTWARE=1 LP_NUM_THREADS=2 \
  ./gradlew --offline --no-daemon -Pedition=unified -PpetOnly runUltimateSmokeClient
ALIEN_SHOWCASE_DISPLAY=:121 bash scripts/record-showcase.sh
```

Minecraft 1.8.9 / Forge 11.15.1.2318 / Java 8u472. O display Xorg usa scripts/ci-combat-xorg.conf, `-noreset -nolisten tcp -ac`. O gravador aqueceu o cenário antes da captura contínua.

GPU física, gameplay em Windows pessoal, autenticação/licença Microsoft interativas, multiplayer externo e OptiFine não foram validados. Emotes e mascotes continuam locais, sem sincronização de rede. Não há áudio funcional neste ambiente.

As sessões General/Scene/Studio ainda registram tarefas de chunks ao descarregar mundos; contagens de pilhas nos logs desta rodada: `{"general": 72, "sky": 0, "combat": 0, "ultimate": 0, "scene": 77, "studio": 79}`. Não são contagens de quedas do jogo; todas as seis sessões terminaram com PASS. [Ocorrências selecionadas](https://github.com/AttackAlien/Alien-Client/blob/372c4b98ffe37d1d81032f154e8ebcb952069a39/captures/atual/unified/known-log-errors.txt), logs completos nos artifacts do CI.

## Acesso público conferido

Quinze arquivos foram baixados novamente das URLs públicas, sem autenticação: MP4, ZIP do launcher, JAR, fotos e GIFs dos dois modelos. Todos retornaram HTTP 200 e SHA-256 idêntico ao arquivo validado. [URLs imutáveis e hashes](mascot-results/2026-11-05/public-download-check.json).
