# Mascote alien animado — Unified 0.11.0-alpha.1

Abra **Cosmetic Studio → Pet** e ative **Mascot**. O mini alien usa um modelo original arredondado, com olhos que piscam, antenas flexíveis, braços e pés animados. O botão Player view alterna entre a prévia ampliada do mascote e a vista junto ao jogador.

- **Follow:** acompanha o dono com movimento amortecido; distância, altura e lado são ajustáveis.
- **Shoulder:** fica junto ao ombro, com proporção reduzida. Distância e altura ficam fixas nesse modo e seus sliders são desabilitados.
- **Orbit:** circula o dono; distância controla o raio e Animation speed altera a velocidade.
- **Alien, Mint e Solar:** paletas originais, com seletor HSV/HEX independente para corpo e detalhes.
- **Behavior:** animação, reação aos emotes, repouso após seis segundos parado e lado do companheiro. Movimento real desperta o mascote; um emote real faz ele acenar.

Cores, modo, tamanho, posição e preferências ficam nos perfis Unified e nos três slots de looks. Perfis antigos começam com o mascote desligado. Os overrides locais por UUID também podem ter sua própria aparência; a reação a emotes usa o playback do jogador local.

É um companheiro visual local, sem criar entidades ou enviar comandos ao servidor. Ele não possui inventário, combate ou navegação física. As posições e os estados transitórios não são salvos. Mudança de mundo/perfil, teleportes, invisibilidade, morte, distância e o master de cosméticos são tratados pelo renderer.

O limite é de quatro mascotes visuais, subordinado ao orçamento de cosméticos, com prioridade para o jogador local. Orçamento zero e Mascot OFF limpam a simulação. Desligar animações mantém o modelo estático. A geometria é compartilhada e usa o cache limitado já existente de meshes.

O release publicado 0.10 continua disponível. Esta implementação está na branch do mascote até a revisão do novo PR.
