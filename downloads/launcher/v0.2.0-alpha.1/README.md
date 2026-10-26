# Alien Launcher 0.2.0-alpha.1 — Windows x64

**[Baixar o ZIP portátil](https://github.com/AttackAlien/Alien-Client/raw/refs/heads/feature/launcher-unified-alpha2-20261026/downloads/launcher/v0.2.0-alpha.1/AlienLauncher-Windows-x64-0.2.0-alpha.1.zip)** · [SHA-256](SHA256SUMS.txt) · [Fotos reais atuais](../../../captures/launcher-atual/README.md) · [Resultados](../../../docs/LAUNCHER-VALIDATION.md)

O ZIP tem aproximadamente **66,45 MiB** e inclui .NET, executável x64, licenças e o **Alien Unified 0.6.0-alpha.2 verificado**. Não é APK/Bedrock. Candidato do [PR #19](https://github.com/AttackAlien/Alien-Client/pull/19), sem merge na main ou release estável.

1. Extraia **todo** o ZIP para uma pasta no Windows 10/11 x64.
2. Abra `AlienLauncher.exe`, mantendo `payload` ao lado do executável.
3. Clique **Instalar / Reparar** para baixar os arquivos oficiais, Forge e Java. A instalação inicial requer internet.
4. Clique **Entrar com Microsoft** e use uma conta que possua Minecraft Java. O login requer Microsoft Edge WebView2 Runtime.
5. Escolha **Jogar Alien**. O launcher observa o processo e só confirma o Alien carregado quando recebe a mensagem real do mod.

RAM, Java 8 e seleção de perfil são salvos. **Cancelar**, **Sair da conta** e **Ver diagnóstico** ficam na janela principal. Dados próprios em `%LOCALAPPDATA%\AlienLauncher`; saves e outros mods são preservados. O Alien continua no **Forge 1.8.9 / Java 8**; versões modernas do seletor são Vanilla separado.

Validação: **22 testes em Linux + 22 em Windows, 38 verificações da janela WinForms real e instalação real Forge no Windows**. Também houve execução real do JAR no Forge/Linux com Java automático e captura de vídeo. **O login Microsoft interativo e gameplay em PC Windows físico continuam sem validação**; leia os limites no relatório antes de tratar a alpha como pronta para uso diário. O EXE não tem assinatura Authenticode.

Código do launcher `fc9edb86a8dc91d8e0ca36606ac302b32471d662`, [CI 40324521453](https://github.com/AttackAlien/Alien-Client/actions/runs/40324521453). Origem e hash do client em [client-source.json](client-source.json).

SHA-256 do ZIP: `057b457cfd7a74b68dd1a4184d623162acfda43ac223be0b13db76a336a6730c`.
