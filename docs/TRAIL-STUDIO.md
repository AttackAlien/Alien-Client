# Trail Studio — Unified 0.7.0-alpha.1

O Trail Studio amplia a Ribbon existente com textura e três pontos de cor, largura e transparência. Os pontos correspondem à **cauda mais antiga**, ao **meio** e à **ponta mais nova** da trilha. A interpolação entre eles é linear; não é um editor de pintura ou de curvas com pontos ilimitados.

## Usar

1. Abra **Right Shift → Cosmetic Studio → Motion → Trail settings**.
2. Selecione **Style / Ribbon** e ative **Trail** e **Designer**.
3. Escolha um preset **Nebula**, **Ember** ou **Glacier**, ou edite o desenho atual.
4. Selecione **Tail / Middle / Head** e use o botão de cor para abrir o HSV/HEX existente.
5. Em **Width curve** ou **Opacity curve**, arraste os três pontos. Clique com o botão direito na área da curva para digitar um valor exato do ponto escolhido.
6. Ajuste largura base, duração, opacidade global, repetições da textura por bloco e velocidade de rolagem.
7. Veja o material na prévia e ande em terceira pessoa para observar a trilha no mundo. **Done** e o fechamento do editor salvam as alterações no perfil ativo.

Largura dos pontos varia de **0 a 1,5×** a largura base; opacidade de **0 a 1**. A opacidade é combinada com a transparência do PNG, a opacidade global e o desaparecimento natural por idade. A opção de animações do client/cosmético precisa estar ativa para produzir trilhas no mundo. A prévia fica estática quando as animações estão reduzidas.

## Texturas próprias

Quatro materiais originais estão disponíveis: **Soft**, **Silk**, **Dashes** e **Weave**. São máscaras pequenas geradas pelo código e coloridas pelo gradiente.

Use **PNG folder**, copie seu arquivo para `config/alien-client/cosmetics/trails/` na instância do jogo e clique **Reload PNGs**. **Texture** percorre os materiais e os PNGs locais encontrados. O botão de pasta também copia seu caminho para a área de transferência quando a operação está disponível.

- PNG local, nome de arquivo simples terminado em `.png`, até **1 MiB**, dimensões de **1 a 1024 px** por eixo.
- RGB do PNG é multiplicado pelo gradiente. Use branco nos três pontos para manter as cores originais de uma textura colorida.
- Arquivo ausente ou rejeitado usa **Soft**, com aviso real no editor. Corrija o arquivo e use **Reload PNGs** para tentar novamente.
- O perfil salva o nome do PNG; o arquivo precisa ser copiado separadamente ao compartilhar o perfil com outra instalação.

## Compatibilidade e renderização

Perfis antigos recebem o Designer **desativado**, mantendo a Ribbon anterior. A opção **Particles** continua disponível e desativa os controles exclusivos da Ribbon. Capas, chapéus, outros cosméticos, HUD, céu, combate e correções de persistência anteriores foram preservados.

O desenho acompanha o acessório selecionado, inclusive overrides locais de jogadores, e faz parte de perfis Unified e looks salvos. As cópias dos desenhos são independentes.

O render usa a trajetória real do jogador, respeita profundidade e desaparece ao desligar, parar além da duração, teleportar ou mudar de mundo. Mantém os limites compartilhados de até oito trajetórias e 192 segmentos por passe; o brilho acompanha o orçamento de performance existente. A textura repete por distância percorrida e não estica quando pontos antigos expiram. Há quatro texturas prontas, oito slots reutilizáveis para PNGs e cache limitado de rejeições, sem decodificar o arquivo a cada quadro.

São visuais locais. Não há sincronização dos cosméticos com outros clientes ou alteração de mecânicas de movimento. As imagens e os resultados da rodada aprovada serão vinculados no relatório de validação.
