# Validação — danças e céu polidos / Unified 0.10.0-alpha.1

Branch `feature/animation-sky-polish-20261104`, [PR #25](https://github.com/AttackAlien/Alien-Client/pull/25), baseada nas auras do PR #24. **Sem merge na main.** [Guia](ANIMATION-SKY-POLISH.md).

## Resultados de 04/11/2026

**223 JUnit**, sem falhas, erros ou ignorados. **763 checks gráficos** em seis sessões reais do Forge. **12 testes de empacotamento** incluindo rejeição de evidências obsoletas.

| Sessão Forge | Resultado |
| --- | --- |
| general | PASS 161 checks |
| sky | PASS 37 checks |
| combat | PASS 24 checks |
| ultimate | PASS 328 checks |
| scene | PASS 71 checks |
| studio | PASS 142 checks |

Legit e Full: 223 JUnit e 155 checks gráficos por composição. Sky Studio: 223 JUnit e 37 checks por composição. Launcher: 31 testes Linux, 31 Windows, 46 checks da janela nativa, seis PNGs e instalação real isolada de Java 8/Forge/JAR 0.10 aprovados. ZIPs com CRC completo; EXE PE/AMD64 e JAR idêntico ao CI verificados.

A sessão completa local também passou 328 checks; a rodada direcionada do céu passou 22 checks. Os checks incluem estados, controles e capturas, não centenas de golden images ou um benchmark. [Recibos originais e XMLs](polish-results/2026-11-04/).

## Danças e compatibilidade

Floss, Orange Justice e Take the L renderizaram no mundo e no Emote Studio com cotovelos e joelhos articulados, incluindo armadura, segunda camada e materiais Glass/Skeleton. O contador real de geometria confirmou membros articulados no render. A chave Articulated limbs editou a preferência real, sem remover as danças. Movimento, ataque, dano, menus e troca de perfil continuaram interrompendo o playback; câmera e coordenadas reais foram preservadas.

Os testes JUnit verificam limites/valores finitos das juntas, continuidade ao repetir os três clássicos, blend de troca/parada, cópias independentes e persistência da preferência. O balanço do corpo usa a fração do frame, além das curvas suavizadas dos membros. O vídeo também exercitou o modelo slim; a sessão local de prévias mostrou o modelo clássico.

## Atmosfera realista

O shader GLSL 1.20 compilou no jogo e produziu capturas de dia, pôr do sol e noite. Alterar o horário visual não mudou o horário do servidor integrado. Cobertura zero e um, com movimento de nuvens congelado, produziram mais de 20 mil pixels diferentes na região do céu observada.

O modo Realistic suspendeu as nuvens quadradas por um render hook próprio, sem editar a opção de vídeo do jogador. Desligar o modo restaurou o renderizador anterior e a preferência original. As nuvens desvanecem antes do horizonte para evitar faixas verticais. As estrelas são suavizadas e perdem visibilidade sob as nuvens.

Details abriu o editor real. O handler do slider alterou a cobertura usada pelo shader; salvar/recarregar um perfil restaurou o valor. Rolagem nativa X11 expôs o último controle no layout compacto; Reset restaurou os padrões. Os controles de aparência foram normalizados e persistidos em testes JUnit. Os índices dos 16 estilos anteriores foram preservados; cubemaps e galerias anteriores passaram pelos seus testes existentes. A suavização opcional dos céus anteriores fica desabilitada no editor quando Realistic está selecionado, pois este usa sua própria atmosfera suave.

## Vídeo e capturas atuais

**[Assistir / baixar MP4](../captures/video-atual/Alien-Client-0.10-customizacoes-full.mp4)** — 105.77s, 960×540, H.264/YUV420p, **30 FPS de gravação**, sem áudio. Mediana de renderização **57.6 FPS**, P95 **31.2 ms/frame**, nesta tomada Linux/Mesa por software. O MP4 é a saída original contínua de ffmpeg x11grab, sem reedição, aceleração, sequência de fotos ou interpolação. Não é promessa de desempenho em outra máquina.

Treze capítulos mostram capa, acessórios, trilhas, partículas, céu Realistic, HUD, danças articuladas, auras e Full. Os módulos reais alinharam a câmera com o alvo de treino, consumiram pão/aumentaram a fome, curaram o jogador com poção e colocaram vários blocos durante caminhada em Survival. O fixture prepara condições de fome/vida/inventário no servidor integrado e é identificado nas legendas como demonstração automatizada local; ele não é distribuído no JAR. AutoBridge é ativado separadamente para seu capítulo.

**297 PNGs novos do jogo** substituem a galeria anterior. Uma captura é da janela X11; as demais são originais do framebuffer, copiadas byte a byte do CI aprovado. Seis PNGs novos do launcher vêm da janela WinForms no Windows Server 2022. Os GIFs de emotes, auras, trilhas e capa são conversões dos novos PNGs; os três primeiros usam intervalos registrados. A capa usa 100 ms por frame. A prévia do vídeo é um frame extraído aos 53s. Não há imagens simuladas.

[Manifesto das capturas](../captures/atual/unified/validation.json) · [Medições/checks do filme](polish-results/2026-11-04/video/) · [Hashes do MP4](../captures/video-atual/SHA256SUMS.txt).

## Origem e pacotes

Client e vídeo: fonte `a66a68da0fe525c3389688bedeb84668770ebbf7`, [CI 41850500847](https://github.com/AttackAlien/Alien-Client/actions/runs/41850500847), JAR SHA-256 `e8a12308278af4048b9ac3d1d103014409c51fd293386d761ccb007eb8fe4b6e`.

Launcher 0.3.0-alpha.7: fonte `62539e68395cb45d4affccfcafbcd6dbc73e2184`, [CI 41853665597](https://github.com/AttackAlien/Alien-Client/actions/runs/41853665597), ZIP SHA-256 `4258bf05cfec0b35539ed996a3b54b79fc6cbf3fe73f415b5c48e167248910ca`, 70,791,185 bytes. O layout/emblema aprovados foram mantidos; o pacote fixa o novo JAR do CI aprovado.

[Client](../downloads/v0.10.0-alpha.1/README.md) · [Launcher](../downloads/launcher/v0.3.0-alpha.7/README.md). A publicação de mídia/documentação/downloads usa `[skip ci]` e mantém os fontes de execução validados. [Comparação dos objetos Git](polish-results/2026-11-04/validated-source-trees.json).

## Comandos usados

```bash
export ALIEN_TOOLS_DIR=/workspace/.alien-tools
export GRADLE_USER_HOME=/workspace/Alien-Client/.gradle-user
./gradlew --offline --no-daemon -Pedition=unified test showcaseClasses
DISPLAY=:115 LIBGL_ALWAYS_SOFTWARE=1 LP_NUM_THREADS=2 \
  ./gradlew --offline --no-daemon -Pedition=unified runUltimateSmokeClient
ALIEN_SHOWCASE_DISPLAY=:116 bash scripts/record-showcase.sh
```

Xorg :115 foi iniciado com `scripts/ci-combat-xorg.conf`, `-noreset -nolisten tcp -ac`. O gravador iniciou seu próprio display :116, esperou o cenário/shader aquecer e capturou a janela real. O CI executou os seis runners gráficos versionados. A rodada direcionada usou `-PpolishOnly runUltimateSmokeClient`.

## Rodadas de desenvolvimento

Antes da rodada final, foram corrigidas duas expectativas JUnit antigas que contavam 16 céus, um import ausente no fixture do vídeo e a espera do teste pelo tick posterior à compilação inicial do shader. A inspeção visual também encontrou a sobreposição das nuvens quadradas e faixas no horizonte; ambas foram corrigidas antes do CI aprovado. [Registro local](polish-results/2026-11-04/local-validation.json).

## Limites observados

Minecraft 1.8.9 / Forge 11.15.1.2318 / Java 8u472. GPU física, gameplay em PC Windows pessoal, autenticação/licença/renovação/logout Microsoft, OptiFine e servidores externos não validados. O filme usa identidade de desenvolvimento em mundo local.

Emotes/auras são locais, sem sincronização de danças pela rede. As nuvens são camadas procedurais suaves, sem ray marching volumétrico. O ambiente não tem áudio funcional. As sessões General/Scene registram tarefas de chunks após descarregar mundos: 72 e 80 ocorrências de pilha; não são contagens de quedas do jogo. As seis sessões terminaram com PASS. [Ocorrências selecionadas](../captures/atual/unified/known-log-errors.txt); logs completos nos artifacts.
