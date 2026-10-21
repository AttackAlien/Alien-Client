# Escopo da versão 0.1.0

| Área | Implementado | Limites e extensões pendentes |
| --- | --- | --- |
| Base Forge | Mod apenas de cliente, inicialização, tecla configurável, configuração JSON atômica, backups de dados inválidos | Sem launcher próprio; requer Minecraft/Forge do usuário |
| Interface | Menu inicial opcional, categorias, pesquisa de categorias, gradientes, RGB, transparência, sliders, atalhos, animações, temas JSON | Fonte padrão do jogo; pesquisa não indexa cada opção dos subeditores |
| HUD | Dez tipos de elementos, arraste, escala, alinhamento, grade, prévia, texto, salvamento em perfis | Um elemento de texto personalizado; não há editor de scripts/plugins de HUD |
| Cosméticos | Modelos originais de chapéu japonês/samurai/coroa, kitsune/máscara futurista/óculos, capa/asas/mochila, charm ligado ao braço da mão, aura/trilha | Geometria baseada em caixas; não importa OBJ/glTF/Blockbench nem altera skins |
| Inspector | Seleção de jogadores carregados e UUIDs já salvos, acessórios por jogador, reset individual/global | Sem editor arbitrário de skin; jogador offline não tem prévia |
| Importação | Modelos JSON de caixas, texturas PNG e panoramas equiretangulares PNG locais | Sem download por URL, pacotes ZIP, scripts ou formatos de animação externos |
| Mundo | Temas Galaxy/Aurora/Japanese/Nebula, panorama 360°, gradiente, estrelas, sol/lua, horário visual, neblina | Apenas dimensão 0; discos de sol/lua, sem editor de suas texturas; não altera a iluminação real do terreno |
| Clima | Precipitação visual Server/Clear/Rain/Snow/Storm, intensidade e orçamento | Storm é chuva visual intensa; não cria trovões/raios/áudio; chuva real pode continuar audível no modo Clear |
| Efeitos | Movimento, aterrissagem, trilhas, acessórios animados, animações de interface, roll de câmera e filtro de cor | Não há editor de animação específico de itens nem pipeline de pós-processamento/shaders |
| Desempenho | Limites de partículas/acessórios, distância, descarte de partículas atrás da câmera, cache de modelos/texturas/estrelas, FPS/frame time/P95/memória | Limites são por subsistema e não abrangem partículas vanilla; sem promessas ou resultados comparativos de FPS |
| Perfis | Persistência, import/export, padrões, associação por servidor e restauração global | Associação local; nenhuma configuração enviada ao servidor |

## Compatibilidade com OptiFine

O mod usa as interfaces públicas de Forge 1.8.9 (`LayerRenderer`, `IRenderHandler`, eventos de HUD/câmera/neblina). Não inclui transformadores de bytecode ou arquivos OptiFine. Isso reduz acoplamento, mas não demonstra compatibilidade em execução.

A renderização de céu/clima pode competir com custom skies, shaders ou outros mods que substituem os mesmos handlers. Quando desativados, os handlers anteriores são restaurados. Testar com OptiFine 1.8.9 é opcional e exige uma cópia obtida pelo usuário nos canais autorizados. Essa combinação ainda não foi validada; não redistribuímos OptiFine. Procedimento em [BENCHMARKS.md](BENCHMARKS.md).

## Compartilhamento futuro e opcional

O formato atual já identifica preferências por UUID. Uma extensão poderá compartilhar somente identificadores de modelos aprovados, cores e transformações, com consentimento explícito e opção de desligar. O catálogo deve validar tamanho, hash e licença de cada recurso; recursos desconhecidos usam fallback local. Uma camada de transporte separada deverá ter limites de requisição/cache e não tocar atributos, inventário, skins de terceiros ou regras do servidor. Nenhum serviço de compartilhamento ou tráfego desse tipo está implementado nesta versão.
