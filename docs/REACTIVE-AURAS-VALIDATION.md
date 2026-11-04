# Validação — auras reativas e vídeo / Unified 0.9.0-alpha.1

Branch `feature/reactive-auras-20261104`, [PR #24](https://github.com/AttackAlien/Alien-Client/pull/24), baseada no Emote Studio do PR #23. **Sem merge na main.** [Guia](REACTIVE-AURAS.md).

## Resultados de 04/11/2026

| Verificação | Resultado |
| --- | --- |
| Unified / Java 8 | 216 JUnit, sem falhas, erros ou ignorados |
| Seis sessões Forge | 742 checks: geral 161, céu 37, combate 24, Ultimate 307, Scene 71, Interactive Studio 142 |
| Empacotamento | 12 testes, incluindo rejeição de evidência obsoleta |
| Legit e Full | 216 JUnit e 155 checks gráficos por composição |
| Sky Studio / Legit e Full | 216 JUnit e 37 checks gráficos por composição |
| Launcher | 31 testes Linux, 31 Windows, 46 checks da janela real |
| Instalação Windows CI | Forge 1.8.9 / Java 8 / JAR 0.9.0-alpha.1 em instância isolada |
| Pacotes | CRC completo dos ZIPs, JAR idêntico ao CI e EXE PE/AMD64 verificados |

A sessão completa local também passou 307 checks; a rodada direcionada de auras passou 65. Os checks incluem estados, controles e capturas; não são um benchmark ou centenas de golden images. [Recibos, XMLs e metadados](aura-results/2026-11-04/).

## Auras exercitadas no Minecraft

Classic continuou emitindo as partículas originais. Reactive passou a emitir geometria real, com estados e unidades de desenho observados no renderer. Movimento nativo X11 elevou o canal de movimento e a energia; parar liberou a reação suavemente. A reprodução real de Floss ativou o canal de emote. Meia-noite no servidor integrado ativou Night; desligar a chave pelo editor liberou a reação enquanto o mundo continuava à meia-noite.

Arcane, Neon e Celestial chegaram ao framebuffer com suas respectivas runas, órbitas e halos. A prévia usa o jogador real. Os testes exercitaram HEX pelo handler do seletor, chaves independentes, persistência em perfis/looks, preservação dos emotes, rolagem em layout compacto, orçamento zero, Aura OFF, restauração de perfil e limites de geometria/jogadores. OpenGL e os escopos de render ficaram limpos ao final.

Os testes puros cobrem migração sem ativar Reactive nos perfis antigos, valores inválidos, noite e horário, independência dos gatilhos, suavização/interpolação, independência do número de frames, presets, vértices finitos, transparência das bordas, orçamento de geometria e cópias independentes nos perfis.

## Vídeo contínuo e fluidez medida

**[Assistir / baixar o MP4](../captures/video-atual/Alien-Client-0.9-customizacoes-full.mp4)** — 103.67 segundos, 960×540, H.264/YUV420p, **30 FPS de gravação**, sem áudio.

O arquivo é a saída original do FFmpeg x11grab, copiada byte a byte, sem sequência de fotos, aceleração ou frames gerados/interpolados. O jogo renderizou com mediana de **56.6 FPS**, P95 de **26.2 ms por frame**, nesta tomada Linux/Mesa por software. A medição é do jogo; o FPS de gravação não foi usado como substituto da medição de renderização. Isso não promete desempenho equivalente em outro computador.

Treze capítulos mostram capa, acessórios, trilhas, partículas, céu, HUD, emotes, auras e os módulos Full. Os dados de teste de fome/vida/posição/inventário são preparados pelo servidor integrado; depois os módulos reais executam suas ações. As legendas identificam a demonstração automatizada em mundo local. O fixture não é distribuído no JAR.

As verificações do filme confirmaram alinhamento real da câmera pelo Aimbot, consumo de pão e aumento da fome por Auto Food, cura por Auto Potion e múltiplos blocos colocados por AutoBridge durante caminhada em Survival. AutoBridge é ativado explicitamente para seu capítulo e permanece desligado por padrão no preset Full. A câmera foi mantida voltada para a ponte após o teleporte de preparação.

Abertura, shader e cenário foram preparados antes de iniciar a gravação. A resolução e as opções gráficas disponíveis no jogo foram ajustadas para fluidez, incluindo ausência de blur no menu. [Medições, capítulos e checks do filme](aura-results/2026-11-04/video/) · [SHA-256 do MP4](../captures/video-atual/SHA256SUMS.txt). A imagem de prévia do vídeo é um frame extraído aos 67 segundos, não uma ilustração.

## Origem verificável

Client e vídeo: fonte `93dcd285cfba8aac9b7e5023f4fc1689ed42cb11`, [CI 41842322972](https://github.com/AttackAlien/Alien-Client/actions/runs/41842322972), JAR SHA-256 `2ea2c0d4a786006863600cfd4f569660288014e47b6045692f7e50fc9084a036`. A fonte do fixture gravado coincide com a fonte validada no CI.

Launcher: fonte `331ee2bbf7aa12e18503e4c3e7bc793b489020e8`, [CI 41845407649](https://github.com/AttackAlien/Alien-Client/actions/runs/41845407649), ZIP SHA-256 `fb5d9e3c25bfac64f3c91a3f6e6e296afbe108ab0306d4e067b20ebe83d05147` (70,780,376 bytes). O pacote fixa o JAR do CI aprovado; ferramentas de teste e caches de conta são excluídos.

**290 PNGs novos do jogo** substituem a galeria anterior. Uma captura é da janela X11 e as demais são originais do framebuffer, copiadas sem alteração do CI aprovado. Os seis PNGs do launcher são capturas nativas da janela WinForms no Windows Server 2022. O layout e o emblema aprovados foram preservados.

Os GIFs de auras, emotes, trilhas e capa usam somente PNGs desta rodada. Os intervalos de auras/emotes/trilhas foram registrados; a capa usa 100 ms por frame na conversão. Não há interpolação. Evidências históricas continuam vinculadas às versões e commits correspondentes.

A publicação de documentação, downloads e mídias usa `[skip ci]`; não altera os arquivos de execução validados. O launcher recebeu seu workflow completo no SHA registrado acima. [Objetos Git comparados](aura-results/2026-11-04/validated-source-trees.json) · [Manifesto das fotos](../captures/atual/unified/validation.json).

## Comandos

```bash
ALIEN_TOOLS_DIR=/workspace/.alien-tools GRADLE_USER_HOME=$PWD/.gradle-user \
  ./gradlew --offline --no-daemon -Pedition=unified test runUltimateSmokeClient

ALIEN_TOOLS_DIR=/workspace/.alien-tools GRADLE_USER_HOME=$PWD/.gradle-user \
  bash scripts/record-showcase.sh
```

A rodada local usou Xorg :113 com `scripts/ci-combat-xorg.conf`. O gravador cria o display :114, executa `runShowcaseClient`, espera o cenário pronto e grava o X11 diretamente. O CI compila o fixture e executa as seis sessões gráficas com os runners versionados. O launcher usa testes .NET 8 e publicação WinForms win-x64.

## Limites

Minecraft 1.8.9 / Forge 11.15.1.2318 / Java 8u472. GPU física, gameplay em PC Windows pessoal, autenticação/licença/renovação/logout Microsoft, OptiFine e servidores externos não foram validados. O filme usa uma identidade de desenvolvimento em mundo local, não uma conta Microsoft autenticada.

As auras e os emotes são locais, sem sincronização pela rede. Não há áudio nem luz dinâmica real no terreno. O orçamento de partículas, a distância, as animações e o master de cosméticos limitam o efeito. Perfis antigos mantêm Classic até a opção Reactive ser escolhida.

O runner não tem áudio funcional. Os logs das sessões General/Scene ainda registram tarefas de chunks após descarregar mundos: 81 e 73 ocorrências da pilha nesta rodada; não são contagens de quedas do jogo. Todas as seis sessões terminaram com PASS. [Ocorrências selecionadas](../captures/atual/unified/known-log-errors.txt); logs completos nos artifacts do CI.

## Acesso público

Nove URLs, incluindo o MP4, o ZIP do launcher, o JAR, fotos e GIF, foram baixadas sem autenticação: HTTP 200 e SHA-256 coincidentes com os arquivos verificados. [Recibo](aura-results/2026-11-04/public-download-check.json).
