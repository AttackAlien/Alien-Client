# Alien Launcher 0.2.0-alpha.1 — Windows x64

Launcher experimental para **Alien Client Unified 0.6.0-alpha.2 / Minecraft Java 1.8.9 / Forge 11.15.1.2318**. Incorpora o trabalho do PR #12, preservando o client atual do PR #18. O mod continua exclusivamente em Forge 1.8.9; outras versões do seletor são Minecraft Vanilla em instâncias independentes.

## Usar

1. Extraia o ZIP inteiro `AlienLauncher-Windows-x64-0.2.0-alpha.1.zip`. Mantenha a pasta `payload` junto de `AlienLauncher.exe`.
2. Abra `AlienLauncher.exe` em Windows 10/11 x64. O .NET está incluído no pacote.
3. Escolha Alien ou um perfil Vanilla. **Instalar / Reparar** baixa/verifica arquivos oficiais; não requer mover JAR manualmente.
4. Use **Entrar com Microsoft** e uma conta que possua Minecraft Java. A senha é inserida somente na janela Microsoft. O login requer Microsoft Edge WebView2 Runtime.
5. Clique **Jogar**. O launcher atualiza a sessão, verifica a instalação e o Java, abre o processo e observa seu encerramento. A confirmação de que o Alien carregou depende da mensagem real do mod no log.
6. **Ver diagnóstico** mostra informações sanitizadas. **Cancelar** interrompe operações em andamento; **Sair da conta** remove a sessão do launcher.

## Instalação e atualização

- Instâncias, contas e configurações ficam em `%LOCALAPPDATA%\AlienLauncher`. A `.minecraft` original não é usada para o cache de contas.
- Alien usa `instances/unified-1.8.9`; Vanilla usa `instances/vanilla-<versão>`. Não há injeção do JAR Alien em versões modernas.
- O pacote fixa o JAR do CI aprovado **40136496598**, fonte **32829c5**, client **0.6.0-alpha.2**, SHA-256 `17a1627a25b66423c1cf60bb745ca88b4888c5a376f93cb9cc736ae2e888c6fc`. O manifesto local não pode escolher outro hash arbitrário.
- Atualizar preserva saves, configurações e outros mods. JARs antigos que declaram o modid `alienclient` são movidos para `alien-backups`, incluindo cópias em `mods/1.8.9`; a cópia nova é validada antes de substituir. Falhas restauram arquivos já movidos.
- Java é instalado pela biblioteca e **sua versão real é consultada antes de executar**. Alien exige Java 8; versões Vanilla usam o requisito do metadado oficial. O seletor de Java 8 permite escolher outro executável compatível, sem afetar os perfis Java 17/21.
- RAM e seleção de perfil/Java são salvas localmente. Tokens OAuth são administrados pelo cache local da biblioteca; não copie `accounts.json` para outras pessoas. Diagnósticos removem tokens conhecidos, bearer/JWT e URLs.

## Validação e limites

Há testes .NET do núcleo em Linux/Windows, testes da janela WinForms real e uma instalação real de Forge no CI Windows. O probe de runtime separado serve exclusivamente aos testes e **não é distribuído no ZIP do launcher**. Uma identidade anônima de desenvolvimento usada pelo probe Linux não é uma opção de login do launcher e não valida autenticação Microsoft.

Login Microsoft interativo, conta com licença, multiplayer externo, GPU física, SmartScreen e uso completo num PC Windows pessoal continuam exigindo confirmação real; a compilação de um EXE não comprova esses fluxos. O executável experimental não tem assinatura Authenticode. Os métodos de captura Windows (desktop ou `PrintWindow`) são registrados nos metadados das imagens; não se usam imagens ilustrativas como evidência.

A instalação inicial requer internet e baixa assets oficiais; o pacote não inclui cópia completa do Minecraft. O launcher não oferece login offline/cracked, catálogo de modpacks ou atualização remota automática do próprio executável. Copiar mods de terceiros manualmente permanece possível na instância isolada, sujeito à compatibilidade real.

## Desenvolvimento

.NET SDK 8.0.427. `AlienLauncher.Core` contém instalação, integridade, perfis e Java; `AlienLauncher` contém WinForms/Microsoft OAuth. `AlienLauncher.Tests`, `.UiSmoke` e `.RuntimeProbe` são apenas verificações.

```powershell
# Defina ALIEN_TEST_PAYLOAD para o diretório do JAR/checksum do CI fixado
# e ALIEN_TEST_JAVA8 para o executável Java 8 real.
dotnet test launcher/AlienLauncher.Tests/AlienLauncher.Tests.csproj
dotnet publish launcher/AlienLauncher/AlienLauncher.csproj -c Release -r win-x64 --self-contained true -p:PublishSingleFile=true
```

O ZIP final precisa de `payload/AlienClient-Unified-1.8.9.jar`, `payload/SHA256SUMS.txt` e `payload/client-source.json`. As bibliotecas CmlLib/XboxAuthNet são MIT; seus avisos/licenças e o crédito do client são preservados. Não é um produto oficial Microsoft/Mojang.
