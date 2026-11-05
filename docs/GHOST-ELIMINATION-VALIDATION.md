# Fantasma Neon — validação

Rodada de 05/11/2026, data de apresentação em America/Bahia. Recibos técnicos em UTC. [PR #30](https://github.com/AttackAlien/Alien-Client/pull/30), sobre #29 e #28. Sem merge na main ou alteração do release público.

| Verificação | Resultado | Execução |
|---|---:|---|
| Unified / JUnit | 284 testes, zero falhas | [42071260924](https://github.com/AttackAlien/Alien-Client/actions/runs/42071260924) |
| Forge 1.8.9 / gráfico completo | 1000 checks, incluindo 77 do fantasma | mesmo CI |
| Empacotamento / evidência antiga | 14 testes | mesmo CI |
| Launcher / .NET | 37 Linux + 37 Windows | [42073487220](https://github.com/AttackAlien/Alien-Client/actions/runs/42073487220) |
| Janela nativa / instalação real | 46 checks, seis imagens e instalação isolada Forge/Java 8 | mesmo CI |

Client fonte `116ed2bebc22a9ee5b8deb80f28542d578d65d0b`, JAR SHA-256 `330507dcbfa4108e73e193b308a7a820e0202a63cdf160d827b87c28b067a3b3`. Launcher fonte `4c75d2ec052f8c3ec6f419cdbc66600db9e3edd3`, ZIP SHA-256 `97320f80ffaef170ad766236a97fa66592c19db7e51e27e42b53e4165ad3e525`.

O ensaio gráfico cria zumbis identificados como **ALIEN GHOST TEST**, com vida reduzida, no servidor integrado real. A ação normal de espada do player controller causa a morte; o efeito só nasce após autoria confirmada e morte/remoção recebida pelo client. Não há chamada ao gerador de prévia para obter um resultado positivo de eliminação.

Foram verificados alvo vivo sem efeito, eliminação própria única, geometria no mundo, desprendimento/subida/dissolução, expiração, morte ambiental sem autoria, filtro de mobs, efeito desligado, controles, paletas, modo suave, persistência de perfil e limpeza ao desligar/sair. Reproduzir a prévia no editor nunca aumenta o contador de eliminações.

Os testes Java cobrem migração de perfis, limites/valores inválidos, cópias independentes, persistência conjunta com música/cosméticos, curvas contínuas e independência de FPS, movimento suave, autoria, deduplicação, mudança de mundo e rejeição de chat comum/formatos desconhecidos. A compatibilidade de identidade cobre o protocolo 1.8.9: SpawnMob envia ID/tipo, sem UUID; jogadores mantêm a checagem de UUID, independentemente das classes diferentes no servidor/client.

O renderizador usa buffers reutilizados e preserva os estados gráficos e de arrays/buffer do OpenGL. A cena mantém teste de profundidade e orçamento de efeitos, com estado transitório limitado. Não adiciona entidades, dano ou pacotes de combate. O Afterglow antigo permanece independente.

[Equivalência das árvores de código](ghost-results/2026-11-05/source-equivalence.json): a publicação final conserva as fontes testadas e acrescenta somente documentação, downloads e mídia.

## Mídia real

**407 PNGs atuais do jogo e seis do launcher** substituem a galeria anterior. [Galeria](../captures/README.md) · [Manifesto](../captures/atual/unified/validation.json) · [Hashes](../captures/atual/unified/SHA256SUMS.txt).

As fotos do fantasma no mundo vêm de mortes reais dos alvos de teste; as fotos do editor mostram a prévia identificada. As fotos de música continuam com fonte sintética identificada; não são evidência de Spotify/YouTube reais.

O [vídeo atual](../captures/video-atual/README.md) tem 13.60s, 960×540, captura contínua X11 solicitada a 30 FPS, sem áudio, aceleração, interpolação ou montagem de fotos. [FFprobe](ghost-results/2026-11-05/video-ffprobe.json) · [Proveniência](ghost-results/2026-11-05/video-provenance.json). Fonte do vídeo `116ed2bebc22a9ee5b8deb80f28542d578d65d0b`; SHA-256 `23e6fe213cc865fd5d6199944f3ab6212c638c47a2164c8537bfb7effc798a56`. A mídia anterior foi retirada das pastas atuais e permanece no histórico Git.

## Limites e reprodução

- Testes gráficos em Linux/Xorg/Mesa por software, com mundo integrado descartável. Não são um benchmark de GPU física.
- Sem validação em multiplayer externo. Mensagens PvP vanilla são reconhecidas e há checagens de autoria/identidade; feeds personalizados apenas em texto não são interpretados. O efeito pode não disparar nesses servidores.
- Não houve login Microsoft interativo ou gameplay em Windows pessoal. O pacote Windows tem teste nativo de UI e instalação.
- O fantasma é uma forma estilizada original; não replica a skin da vítima.
- As contagens de exceções de tarefas de chunks nos demais ensaios de troca/encerramento de mundos foram preservadas em [known-log-errors.txt](../captures/atual/unified/known-log-errors.txt), sem apresentá-las como contagem de crashes.

```bash
ALIEN_TOOLS_DIR=/workspace/.alien-tools \
GRADLE_USER_HOME=/workspace/Alien-Client/.gradle-user \
./gradlew --offline --no-daemon -Pedition=unified test build

ALIEN_EDITION=unified bash scripts/ci-ghost-smoke-xorg.sh
```

`runGhostSmokeClient` usa um mod de teste excluído do JAR distribuído. A gravação usa `-PghostVideo` para evitar a escrita de uma sequência de PNGs durante a captura; o ensaio normal salva os framebuffers e verifica OpenGL. Configuração e comandos do gravador ficam no recibo de proveniência.

[Guia](GHOST-ELIMINATION.md) · [Recibos](ghost-results/2026-11-05/) · [JAR candidato](../downloads/v0.13.0-alpha.1/README.md) · [Launcher candidato](../downloads/launcher/v0.3.0-alpha.11/README.md).
