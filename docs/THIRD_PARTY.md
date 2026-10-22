# Dependências e distribuição

O código dos acessórios e a maior parte da interface foi criado para este projeto. A partir da experiência experimental de UI do PanelStudio, duas classes Java 8 (Animation e SettingsAnimation) são reutilizadas sob licença MIT, com atribuição e licença integral em `docs/licenses/PANELSTUDIO-MIT.txt` e no JAR independente em `META-INF/licenses/PANELSTUDIO-MIT.txt`. Não foram copiados recursos ou implementações do Lunar Client, GameSense/Skeet ou outros clientes proprietários. Os nomes no pedido servem como referências de objetivos e não indicam associação.

As malhas curvas de chapéus/máscaras/acessórios, a deformação da capa, os emblemas Alien/Moon/Stars, as formas de partículas e as paletas são gerados pelo código do projeto. O emblema Alien usa um desenho próprio. Referências visuais não autorizam reproduzir logotipos, skins, modelos, texturas ou identidade de terceiros; nenhum vídeo, frame ou mídia de redes sociais integra esses recursos.

O JAR Alien contém somente o código e os recursos do mod. Minecraft, Forge, OptiFine, skins e bibliotecas externas não são incorporados nele. Usuários precisam obter Minecraft e Forge pelos canais autorizados e cumprir as regras dos servidores onde jogam.

| Componente | Uso | Licença/origem |
| --- | --- | --- |
| Minecraft Java 1.8.9 | Ambiente alvo, baixado pelo ForgeGradle para desenvolvimento | Proprietário, Mojang/Microsoft; não redistribuído |
| Minecraft Forge / ForgeGradle | API e build | Projetos MinecraftForge, LGPL; dependências externas |
| Gradle wrapper | Inicialização do build | Apache License 2.0; avisos junto ao wrapper |
| Eclipse Temurin/OpenJDK 8 | Ferramenta local de compilação | GPLv2 com Classpath Exception; avisos na distribuição baixada |
| Gson | Configuração JSON, fornecido pelas dependências de jogo | Apache License 2.0 |
| LWJGL 2 | API gráfica/input do jogo | BSD; fornecido pelas dependências de jogo |
| JUnit 4 | Somente testes | Eclipse Public License 1.0 |
| PanelStudio (Animation e SettingsAnimation) | Interpolação temporal dos botões ClickGUI, código-fonte incluído | MIT, © lukflug e colaboradores; [aviso integral](licenses/PANELSTUDIO-MIT.txt) |

O build e os testes usam os avisos e licenças contidos nas distribuições de cada dependência. Ao distribuir novos modelos/texturas, preserve seus avisos e confirme que a licença permite o uso. Nenhum arquivo OptiFine faz parte deste projeto ou dos pacotes.

As capturas de validação registram o Minecraft efetivamente executado e seus recursos fornecidos pela instalação de desenvolvimento. Elas são evidência de teste separada; não são usadas como texturas da interface nem como modelos do mod. A versão 0.2.0 não adiciona dependência externa para a nova geometria, o layout dos painéis ou as partículas.

## Fonte da interface 0.4

Open Sans Regular e Semibold, dados digitalizados copyright 2010–2011 Google Corporation, sob Apache License 2.0. Os binários vieram do pacote Debian `fonts-open-sans`, sem modificações. Licença e aviso são distribuídos em `assets/alienclient/fonts/LICENSE.txt` e `NOTICE.txt`; os atlas são gerados em tempo de execução. Os ícones e desenhos da interface Alien são vetores originais.
