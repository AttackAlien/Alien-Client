# Alien Launcher — Windows 0.1 alpha

Launcher independente de interface para o **Alien Client Unified 0.4** (Minecraft 1.8.9 / Forge 11.15.1.2318). Esta é uma versão experimental; não é um produto oficial da Mojang, Microsoft ou Minecraft.

## O que faz

- Possui interface própria **Alien Launcher**, com botões **Entrar com Microsoft**, **Instalar / Atualizar**, **Jogar**, **Abrir pasta de mods** e **Atualizar lista de versões**.
- O seletor oferece **Alien Client 0.4 / Forge 1.8.9** e perfis Vanilla de outras versões: 1.8.9, 1.12.2, 1.16.5, 1.20.1 e 1.21.1 já visíveis; **Atualizar lista** consulta o catálogo de versões release da biblioteca Minecraft para disponibilizar outras versões oficiais (sem snapshots, nessa primeira fase).
- Autentica uma conta legítima de Minecraft Java por meio da janela Microsoft OAuth da biblioteca CmlLib. **Não peça, insira ou armazene senhas no Alien Launcher**, e não compartilhe arquivos de conta.
- Instala bibliotecas oficiais do Minecraft 1.8.9 e a versão exata Forge **11.15.1.2318**, iniciando o processo de jogo diretamente a partir do launcher.
- Entrega no ZIP um `AlienClient-Unified-1.8.9.jar` já compilado e validado pelo workflow oficial do repositório; verifica seu SHA-256 antes de copiar para a instância.
- Mantém toda a instância em `%LOCALAPPDATA%\AlienLauncher\instances\unified-1.8.9`, sem escrever na pasta `%APPDATA%\.minecraft` que você já utiliza.
- Permite RAM entre 1024 e 16384 MB, por padrão 4096 MB.

## Como executar

1. Baixe o ZIP **AlienLauncher-Windows-x64-0.1-alpha.zip** dos artefatos do workflow **Alien Launcher | Windows EXE**, em uma execução verde correspondente a este commit.
2. Extraia o ZIP inteiro para uma pasta de sua escolha. **Não retire o EXE da pasta**: a subpasta `payload` contém o mod e seu checksum.
3. Execute `AlienLauncher.exe` no Windows 10/11 x64.
4. Clique em **ENTRAR COM MICROSOFT**. Use apenas a janela oficial de login Microsoft; é necessário possuir Minecraft: Java Edition.
5. Em **VERSÃO DO JOGO**, escolha **Alien Client 0.4 (Forge 1.8.9)** ou uma das versões Vanilla. Use **ATUALIZAR LISTA** para mostrar outras versões release oficiais.
6. Clique em **INSTALAR / ATUALIZAR**. A primeira instalação baixa Minecraft, Java e bibliotecas necessárias; internet é necessária. Forge é instalado apenas para Alien 1.8.9.
7. Clique em **JOGAR ALIEN** (perfil Alien) ou **JOGAR MINECRAFT** (perfil Vanilla).
7. O botão de pasta abre o `mods` exclusivo desta instância para instalar outros mods Forge 1.8.9 que você queira testar.

## Limitações e segurança

- O arquivo do mod **continua existindo internamente** porque Minecraft Forge só carrega mods desta maneira. Você não precisa mover ou instalar o JAR manualmente.
- **Selecionar Minecraft Vanilla 1.12/1.16/1.20/1.21 NÃO significa Alien Client nessas versões.** O código e as funcionalidades do Alien continuam exclusivamente na versão Forge 1.8.9; portar o mod para outras versões é um projeto separado.
- O seletor aceita apenas identificadores numéricos de releases oficiais, nunca nomes arbitrários que poderiam escrever pastas inesperadas.
- Login Microsoft via CmlLib depende de **Microsoft Edge WebView2 Runtime**. O backend faz o OAuth sem pedir a senha ao próprio aplicativo. A compatibilidade exata de login depende dos serviços Microsoft.
- O EXE é **experimental e ainda não tem assinatura de código**; SmartScreen pode pedir confirmação. Verifique a origem no próprio GitHub e os hashes antes de executá-lo.
- O workflow valida compilação de um EXE nativo Windows, origem do JAR, SHA-256 e conteúdo da distribuição. **Não comprova jogo jogável num PC físico, login real, Windows + GPU, OptiFine ou compatibilidade com mods terceiros.**
- Forge 1.8.9 pode exigir Java 8; caso a biblioteca não selecione uma versão compatível, será necessário diagnosticar o Java instalado.
- A instalação inicial depende de downloads de Mojang/Microsoft/Forge; **não distribui uma cópia de Minecraft completo nem contorna autenticação**.
- O pacote vincula-se intencionalmente ao build validado da `main` no commit `933f93ef3498f204ee3858c78785fd4b7992d937`, execução CI `39956674445`. Atualizações futuras exigem novo build/manifesto verificado.
- O suporte a atualizações remotas automáticas do Alien, canais de versão, modpacks Forge para outras versões, lista/instalação de mods e temas avançados fica para versões seguintes.

## Tecnologia / como compilar

Frontend: C# WinForms, .NET 8, Windows x64. Backend: `CmlLib.Core`, `CmlLib.Core.Auth.Microsoft` e `CmlLib.Core.Installer.Forge` com versões fixadas no `csproj`, licenças de terceiros nos respectivos repositórios.

```powershell
dotnet restore launcher/AlienLauncher/AlienLauncher.csproj
dotnet publish launcher/AlienLauncher/AlienLauncher.csproj -c Release -r win-x64 --self-contained true -p:PublishSingleFile=true
```

O executável publicado precisa do subdiretório `payload` criado no workflow de CI. O código-fonte pode ser compilado livremente; **não embarque um JAR não validado** e não exponha arquivos de login.

Para o teste inicial: registrar FPS médio/P95/P99 em mundos locais idênticos, conferir o HUD/Cosmetic/Sky Studio, testar reinicialização e adicionar mods um por vez na pasta isolada. Não habilitar assistência de combate em servidores que a proíbem.
