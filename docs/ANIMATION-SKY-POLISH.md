# Danças articuladas e céu realista — 0.10.0-alpha.1

Floss, Orange Justice e Take the L agora usam cotovelos e joelhos articulados, com curvas contínuas e transições de entrada, troca e saída suavizadas. Pele, segunda camada e armadura acompanham as juntas; os itens segurados e o charm acompanham o antebraço. As coordenadas reais do jogador e a câmera continuam independentes da dança.

Abra **Emote Studio → Settings → Articulated limbs** para alternar a articulação. OFF mantém as danças usando os membros inteiros; jogadores sem emote usam a geometria vanilla. A preferência fica nos perfis Unified. Movimento, ataque, dano e troca de perfil continuam interrompendo os emotes.

Em **Scene Studio → Sky → Realistic**, o céu usa gradiente natural, duas camadas de nuvens suaves em movimento, luz de dia/crepúsculo/noite e estrelas suavizadas, ocultadas pelas nuvens. São camadas procedurais de ruído, sem ray marching volumétrico ou dependência de shader packs.

**Details** abre o editor de atmosfera com prévia real: cobertura e suavidade das nuvens, névoa do horizonte, densidade de estrelas e velocidade. As opções ficam nos perfis Unified. A hora vem do mundo ou do override visual do World Studio; o céu não altera o relógio do servidor. Cobertura zero remove as nuvens procedurais. O modo Realistic suspende as nuvens quadradas enquanto o shader estiver disponível e restaura o renderizador anterior ao desligar, sem editar as opções de vídeo do jogador.

Os 16 céus anteriores continuam selecionáveis com os mesmos índices. Smooth skies suaviza nuvens e estrelas dos estilos procedurais anteriores; OFF conserva sua aparência anterior. Os cubemaps importados do Sky Studio continuam independentes.

Não foram usados assets extraídos do Fortnite. Os emotes são adaptações procedurais locais e não sincronizam danças com outros jogadores.
