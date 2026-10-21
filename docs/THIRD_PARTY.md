# Dependências e distribuição

O código dos acessórios e da interface foi criado para este projeto. Não foram copiados recursos ou implementações do Lunar Client, GameSense/Skeet ou outros clientes proprietários. Os nomes no pedido servem como referências de objetivos e não indicam associação.

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

O build e os testes usam os avisos e licenças contidos nas distribuições de cada dependência. Ao distribuir novos modelos/texturas, preserve seus avisos e confirme que a licença permite o uso. Nenhum arquivo OptiFine faz parte deste projeto ou dos pacotes.
