# Now Playing — HUD de música

O Unified 0.12 adiciona um widget opcional ao HUD Studio Ultimate. Ele mostra o título, artista e capa que o aplicativo entrega ao Windows. A duração, o tempo decorrido e a barra de progresso aparecem quando o aplicativo fornece esses dados.

## Usar no Windows

1. Extraia o launcher candidato completo e abra `AlienLauncher.exe` no Windows 10 **1809 ou posterior**, ou Windows 11, x64.
2. Instale/repare o perfil Alien 1.8.9, conecte sua conta Microsoft com Minecraft Java e entre no jogo.
3. Abra **HUD Studio Ultimate**, selecione **Now Playing** na lista e use **Toggle** para ativá-lo. Use **Next** ou role a lista se estiver abaixo da área visível.
4. Inicie uma música no Spotify ou em um navegador que publique uma sessão de mídia do Windows. No YouTube, isso depende do suporte/configuração do navegador. Uma aba aberta sem reprodução pode não criar uma sessão.
5. Em **Options**, escolha o layout e os elementos visíveis. Arraste o widget no editor; use os controles de escala e alinhamento existentes.

Não é necessário autenticar o Spotify no client. O launcher oferece a ponte de mídia ao processo do Minecraft; a ponte só inicia quando o widget está ativo no mundo ou selecionado para prévia no editor. Iniciar o JAR por outro launcher não disponibiliza essa ponte automaticamente. Nesse caso o editor informa **Abra pelo Alien Launcher**.

## Personalizar

- **Compact**, **Card** ou **Transparent**.
- Capa, artista, barra de progresso, relógio e nome do aplicativo, individualmente.
- Largura de 180 a 360 unidades de interface; escala de 0,5 a 3, posição, alinhamento e opacidade do widget.
- Cor do tema de HUD ou um destaque próprio pelo seletor existente.
- Rolagem de títulos longos, respeitando o recorte do editor e a opção global de animações.
- Ocultar quando pausado ou inativo.
- Fonte **Auto**, **Spotify** ou **Browser**. Auto prioriza uma sessão em reprodução; o filtro permite escolher apenas Spotify ou navegadores reconhecidos.

A aparência e a seleção de fonte são salvas nos perfis Unified e na exportação de layouts de HUD. Os quinze widgets anteriores continuam disponíveis. Now Playing começa desligado e não altera módulos de combate, entradas do jogador ou pacotes.

## Comportamento real

Ao pausar, o relógio congela. Trocas de faixa e buscas usam a nova posição fornecida pelo aplicativo. Sem duração conhecida, a barra desaparece. Sem posição conhecida, o relógio não inventa um valor. Após seis segundos sem atualização, os dados expiram; o widget se oculta ou mostra um estado vazio, conforme a preferência.

O nome da fonte identifica o aplicativo: **Spotify**, **Chrome**, **Edge**, **Firefox**, **Brave** ou **Reprodutor**. A API não identifica de forma confiável o site aberto em cada aba; por isso uma sessão do Chrome não é renomeada como YouTube.

## Dados e implementação

A ponte consulta `Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager`. Não lê títulos de janelas, histórico do navegador, credenciais nem áudio. Não controla a reprodução e não envia os metadados pela rede.

A sessão atual fica temporariamente em `config/alien-client/media-runtime/session-*`, dentro da instância do jogo. O arquivo de configuração contém somente preferências. Desligar o widget encerra a ponte e remove os arquivos temporários; a ponte também observa o encerramento do processo do Minecraft.

Processos, leitura de JSON e decodificação de capas ficam em uma thread de trabalho. O renderizador usa um snapshot imutável e mantém no máximo uma textura de capa. Há limites de tamanho/profundidade para JSON e imagens, e a capa precisa corresponder ao checksum do snapshot para evitar misturar duas faixas.

## Validação

[Resultados e limitações](NOW-PLAYING-VALIDATION.md) · [Capturas atuais](../captures/README.md) · [PR #29](https://github.com/AttackAlien/Alien-Client/pull/29).

O teste gráfico usa uma fonte sintética explicitamente identificada. O teste nativo usa um publicador real de diagnóstico do Windows SMTC. Nenhum deles equivale a uma sessão de Spotify ou YouTube autenticada; a compatibilidade com aplicativos reais deve ser confirmada no computador de uso.
