# Alien Launcher 0.2.0-alpha.1 — validação de 26/10/2026

O launcher do PR #12 foi integrado ao **Unified 0.6.0-alpha.2 do PR #18**, na branch `feature/launcher-unified-alpha2-20261026`, [PR #19](https://github.com/AttackAlien/Alien-Client/pull/19). **Sem merge na main.** O código Java do client e suas funcionalidades foram preservados.

[Download Windows x64](../downloads/launcher/v0.2.0-alpha.1/README.md) · [Capturas reais atuais](../captures/launcher-atual/README.md) · [CI final aprovado](https://github.com/AttackAlien/Alien-Client/actions/runs/40324521453) · [Instruções e arquitetura](../launcher/README.md)

## Estado analisado e mudanças

O PR #12 estava aberto/em rascunho, com EXE compilado, mas ainda fixava o client 0.4. Não havia comprovação de login Microsoft interativo ou gameplay no Windows. Sua implementação considerava a criação do processo uma inicialização bem-sucedida e o SDK de contas usava, por padrão, o cache da `.minecraft` original.

A integração usa o mesmo núcleo do client atual, sem modificar Java: origem imutável do JAR, hash fixo no código, metadados Unified/versão verificados, Java real consultado antes de abrir o jogo e confirmação pelo log de inicialização do Alien. O cache de contas, saves e perfis ficam isolados em `%LOCALAPPDATA%/AlienLauncher`.

Atualizações fazem backup de JARs Alien anteriores, inclusive em `mods/1.8.9`, preservam outros mods/saves/configurações e restauram arquivos movidos se a cópia final falhar. Um JAR já correto permanece intacto nas próximas aberturas. Há cancelamento, logout, diagnóstico sanitizado, escolha de Java 8 e preferências de RAM/perfil. O instalador Forge usa a API pública da biblioteca diretamente, evitando abrir seu redirecionador de anúncios.

## Resultados realmente obtidos

Código do launcher validado: **`fc9edb86a8dc91d8e0ca36606ac302b32471d662`**. Os commits posteriores desta entrega acrescentam documentação, capturas e o ZIP exato do CI.

| Verificação | Resultado e limite |
| --- | --- |
| Núcleo .NET em Linux | **22/22**, zero falhas ou testes ignorados. [TRX](launcher-results/2026-10-26/linux.trx). |
| Núcleo .NET em Windows Server 2022 | **22/22**, incluindo Java 8 executado de verdade. [TRX](launcher-results/2026-10-26/windows.trx). |
| Interface WinForms no Windows | **38 verificações**, janela de produção aberta, limites dos controles, títulos sem sobreposição, perfil Vanilla real, persistência de RAM/perfil, diagnóstico e três capturas do desktop. [Resultado](launcher-results/2026-10-26/windows-ui.txt). |
| Instalação Windows limpa | Downloads reais Minecraft/Java/Forge, perfil Forge esperado e implantação do JAR verificado. **Passou**. Não abriu o Minecraft nesse teste Windows. [Log](launcher-results/2026-10-26/windows-install.txt). |
| Runtime Linux com Java automático | **Passou**: JAR de distribuição, Forge 1.8.9, Java 8 gerenciado, mensagem real `Alien Client 0.6.0-alpha.2 initialized`, seguido de 20 segundos com processo ativo. Encerramento forçado pelo probe ao final. [Log](launcher-results/2026-10-26/linux-runtime.txt). |
| Vanilla 1.21.1 separado | **Instalação passou** e confirmou Java 21 a partir do metadado oficial. Um download inicialmente terminou antes da hora; repetir a instalação completou os arquivos. Não houve teste gráfico do Vanilla moderno. [Log da repetição](launcher-results/2026-10-26/linux-vanilla-1.21.1-install.txt). |
| Integridade do pacote final | ZIP lido integralmente, CRC de todas as entradas válido, EXE PE/AMD64, JAR idêntico ao hash fixado, manifesto de origem equivalente e ausência dos executáveis de teste/caches de conta. **Passou**. |

Os 22 testes cobrem rejeição de caminhos/versões inválidos, isolamento, configurações corrompidas, Java incompatível, remoção de credenciais de diagnóstico, JAR real, adulteração conjunta de JAR/checksum, preservação/backup/rollback, deploy pré-cancelado e ausência de backups repetidos. As 38 verificações da interface incluem os limites de 23 controles; não equivalem a 38 fluxos de usuário completos.

Os relatórios de texto versionados normalizam finais de linha e espaços finais para o Git. Os originais permanecem nos artifacts do CI; imagens, vídeo e ZIP foram copiados sem alteração.

O código Java não mudou nesta tarefa. Os **175 JUnit** aprovados no CI de origem do client foram reutilizados como evidência do JAR fixado; não são testes .NET nem uma nova execução JUnit deste launcher. As verificações gráficas anteriores do client continuam documentadas em [INTERACTIVE-STUDIO-VALIDATION.md](INTERACTIVE-STUDIO-VALIDATION.md).

## Capturas e vídeo

[Galeria atual](../captures/launcher-atual/README.md): três PNGs da janela Windows, um PNG do Minecraft e vídeo real de inicialização de aproximadamente 30 segundos. Foram copiadas as capturas originais, sem montagem, ilustração ou geração por IA. As imagens Windows vieram de `Graphics.CopyFromScreen` às **09:44 de 26/10/2026 na Bahia / 12:44 UTC**; o fallback `PrintWindow` disponível no teste não foi necessário nessa rodada. [Metadados e hashes](../captures/launcher-atual/manifest.json).

A captura do Minecraft usa Linux/Xvfb e OpenGL por software. O probe de teste usa uma identidade anônima de desenvolvimento, como os harnesses de mods; **isso não representa autenticação Microsoft**. O probe e essa opção não são distribuídos no launcher de produção. O vídeo mostra carregamento/menu, sem comprovar gameplay, FPS de PC físico ou multiplayer.

## Origem e integridade

- Client **0.6.0-alpha.2**, Minecraft **1.8.9**, Forge **11.15.1.2318**.
- Fonte do client `32829c561b8dc53719a71b7a6621c58cb5a2e8f5`; [CI aprovado 40136496598](https://github.com/AttackAlien/Alien-Client/actions/runs/40136496598).
- JAR SHA-256 `17a1627a25b66423c1cf60bb745ca88b4888c5a376f93cb9cc736ae2e888c6fc`.
- ZIP do launcher SHA-256 `057b457cfd7a74b68dd1a4184d623162acfda43ac223be0b13db76a336a6730c`, **69.676.957 bytes**.

O JAR/checksum/recibo originais estão versionados em `launcher/payload`, evitando depender da retenção temporária do artifact. O build manual anterior da mesma versão do client possui seu próprio checksum; não misture os manifestos dos dois pacotes. Este launcher fixa exatamente os bytes do CI citado. .NET, dependências e licenças estão incluídos no ZIP; Minecraft/Forge/Java são baixados durante a instalação.

## Comandos usados

Núcleo e build:

```bash
export ALIEN_TEST_PAYLOAD="$PWD/launcher/payload"
export ALIEN_TEST_JAVA8=/workspace/.alien-tools/jdk8u472-b08/bin/java
dotnet test launcher/AlienLauncher.Tests/AlienLauncher.Tests.csproj --logger 'trx;LogFileName=launcher.trx'
dotnet build launcher/AlienLauncher.UiSmoke/AlienLauncher.UiSmoke.csproj
dotnet publish launcher/AlienLauncher/AlienLauncher.csproj -c Release -r win-x64 --self-contained true -p:PublishSingleFile=true -p:IncludeNativeLibrariesForSelfExtract=true
```

Execução gráfica Linux pelo núcleo real do launcher, com Java automático (`""`):

```bash
/workspace/.alien-tools/xvfb/root/usr/bin/Xvfb :95 -screen 0 1280x720x24 +extension GLX +render -noreset -nolisten tcp -ac
export DISPLAY=:95 LIBGL_ALWAYS_SOFTWARE=1
dotnet run --no-build --project launcher/AlienLauncher.RuntimeProbe/AlienLauncher.RuntimeProbe.csproj -- /tmp/alien-launcher-runtime-instance /tmp/alien-launcher-source-client/distribution "" --run
ffmpeg -hide_banner -loglevel error -y -f x11grab -draw_mouse 0 -video_size 1280x720 -i :95 -frames:v 1 /tmp/alien-launcher-managed-runtime-menu.png
```

O servidor X e o probe foram executados em processos paralelos; a captura foi solicitada dez segundos depois da confirmação de carregamento. O vídeo usou `ffmpeg -f x11grab -framerate 15 ... -c:v libx264`. O Windows executou `AlienLauncher.UiSmoke.exe <pasta-de-saída>` e o RuntimeProbe sem `--run` para instalar. As etapas completas estão em [alien-launcher.yml](../.github/workflows/alien-launcher.yml).

## Correções encontradas na validação

O primeiro CI, [40323312745](https://github.com/AttackAlien/Alien-Client/actions/runs/40323312745), passou os 21 testes originais nos dois sistemas, mas falhou na verificação de captura do diagnóstico. A amostragem esparsa de cores confundia a janela de poucas cores com imagem vazia; foi substituída por inspeção densa com verificação de conteúdo. A captura Windows real também revelou sobreposição no título, corrigida e coberta pelo teste da janela. O cabeçalho Vanilla passou a refletir a versão selecionada.

Durante o empacotamento local, foi necessário excluir explicitamente o payload do arquivo único .NET para mantê-lo ao lado do EXE. A verificação final confirmou a presença externa do JAR. O CI final acima aprovou as correções e o teste adicional de atualização sem regravação.

## Limites pendentes

**Não foi validado o login Microsoft interativo com uma conta que possua Minecraft Java**, nem renovação/logout de uma sessão real, multiplayer externo, gameplay em Windows físico, GPU física, alto DPI ou SmartScreen. O EXE é uma alpha sem assinatura Authenticode. A janela mostra corretamente conta desconectada nas imagens. Não é uma release estável.

Os logs Linux contêm falhas do Twitch SDK legado e OpenAL/ALSA sem dispositivo de áudio no ambiente virtual; o menu renderizou e o processo permaneceu ativo. Áudio não foi validado. O teste não deve ser descrito como execução sem mensagens de erro.

O launcher ainda não implementa atualização remota automática do próprio EXE ou catálogo de modpacks. As versões modernas do seletor executam Vanilla; o Alien não foi portado para elas. O cache OAuth é administrado pela biblioteca em `accounts.json` na pasta própria do launcher e não integra diagnósticos ou arquivos de distribuição.
