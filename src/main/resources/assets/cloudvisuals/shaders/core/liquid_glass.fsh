#version 330
#moj_import <minecraft:dynamictransforms.glsl>

#moj_import <cloudvisuals:shape.glsl>
#moj_import <cloudvisuals:paint.glsl>

// Angel Visuals — жидкое стекло: преломление у края, линза, «живая» жидкость,
// непрерывная световая полоса (iOS 26), френель, внутренняя тень,
// радужная кромка, цветокоррекция подложки и слияние фигур в одну каплю.

uniform sampler2D Sampler0;     // чёткий снимок экрана
uniform sampler2D Sampler1;     // лёгкое размытие (линейный свет)
uniform sampler2D Sampler2;     // среднее размытие
uniform sampler2D Sampler3;     // сильное размытие

layout(std140) uniform CloudVisualsParams {
    vec2 OutSize;           // размер экрана, px
    float PixelScale;       // экранных пикселей в одном GUI-пикселе
    float Time;             // секунды — для анимаций
    float Shapes[32];       // до 8 фигур: центр x, центр y, полуширина, полувысота
    float Radii[32];        // по 4 радиуса углов на фигуру
    float Corners[32];      // по 4 формы углов на фигуру
    int ShapeCount;
    float Merge;            // радиус слияния фигур, px
    vec4 Bounds;            // x, y, ширина, высота всей стеклянной области
    float Blur;             // 0 — чётко, 1 — сильно размыто
    float Refraction;       // сила преломления у края, px (минус — внутрь)
    float Bevel;            // ширина линзы у края, px
    float Curve;            // профиль линзы: больше — эффект ближе к краю
    float Magnify;          // увеличение в центре (1 — без увеличения)
    float Dispersion;       // радужная кромка
    float Specular;         // яркость бликов
    float Sharpness;        // резкость бликов
    vec3 SpecularColor;
    vec2 LightDir;          // откуда падает свет (x вправо, y вниз)
    float Rim;              // яркость тонкой кромки
    float RimWidth;         // ширина кромки, экранные пиксели
    float Fresnel;          // свечение к краям
    float Saturation;
    float Brightness;
    float Contrast;
    float Grain;            // зерно
    float Wobble;           // «живая» жидкость: амплитуда, px
    float WobbleScale;      // размер волн
    float WobbleSpeed;      // скорость волн
    float Depth;            // затемнение края — ощущение толщины
    int TintType;
    int TintCount;
    float TintLab[16];
    vec4 TintPos;
    vec2 TintDir;
    vec2 TintCenter;
    float TintRadius;
    float Ripples[16];      // до 4 кругов от нажатий: x, y, возраст (с), сила (px)
    int RippleCount;
};

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

float shapeDistance(vec2 p, int i) {
    int o = i * 4;
    vec2 center = vec2(Shapes[o], Shapes[o + 1]);
    vec2 halfSize = vec2(Shapes[o + 2], Shapes[o + 3]);
    vec4 radius = vec4(Radii[o], Radii[o + 1], Radii[o + 2], Radii[o + 3]);
    vec4 corner = vec4(Corners[o], Corners[o + 1], Corners[o + 2], Corners[o + 3]);
    return minee_roundedBox(p - center, halfSize, radius, corner);
}

float scene(vec2 p) {
    float d = 100000.0;
    for (int i = 0; i < ShapeCount; i++) {
        d = minee_smoothMin(d, shapeDistance(p, i), Merge);
    }
    return d;
}

vec2 nearestCenter(vec2 p) {
    float best = 100000.0;
    vec2 center = p;
    for (int i = 0; i < ShapeCount; i++) {
        float d = shapeDistance(p, i);
        if (d < best) {
            best = d;
            center = vec2(Shapes[i * 4], Shapes[i * 4 + 1]);
        }
    }
    return center;
}

// Подложка нужной размытости
vec3 backdrop(vec2 uv) {
    float level = clamp(Blur, 0.0, 1.0) * 3.0;
    if (level < 1.0) {
        return mix(texture(Sampler0, uv).rgb, minee_toGamma(texture(Sampler1, uv).rgb), level);
    }
    if (level < 2.0) {
        return mix(minee_toGamma(texture(Sampler1, uv).rgb), minee_toGamma(texture(Sampler2, uv).rgb), level - 1.0);
    }
    return mix(minee_toGamma(texture(Sampler2, uv).rgb), minee_toGamma(texture(Sampler3, uv).rgb), level - 2.0);
}

vec2 flow(vec2 p) {
    float t = Time * WobbleSpeed;
    vec2 q = p * (0.04 / max(WobbleScale, 0.05));
    return vec2(minee_valueNoise(q + vec2(t * 0.7, t * 0.3)),
                minee_valueNoise(q + vec2(-t * 0.4, t * 0.6) + 19.3)) * 2.0 - 1.0;
}

void main() {
    vec2 p = texCoord0 * OutSize;
    
    // Анимация волн «жидкого стекла»
    if (Wobble > 0.0) {
        p += flow(p) * Wobble;
    }

    float d = scene(p);
    float pixel = max(fwidth(d), 0.0001);
    float mask = clamp(0.5 - d / pixel, 0.0, 1.0);
    if (mask <= 0.0) {
        discard;
    }

    // Нормаль края
    vec2 gradient = vec2(scene(p + vec2(0.5, 0.0)) - scene(p - vec2(0.5, 0.0)),
                         scene(p + vec2(0.0, 0.5)) - scene(p - vec2(0.0, 0.5)));
    float gradientLength = length(gradient);
    vec2 normal = gradientLength > 0.00001 ? gradient / gradientLength : vec2(0.0);

    // Профиль линзы: 1 у края, 0 в глубине стекла
    float distToEdge = -d; 
    float lens = clamp(1.0 - distToEdge / max(Bevel, 1.0), 0.0, 1.0);
    lens = pow(lens, max(Curve, 0.01));

    // Увеличение в центре и преломление
    vec2 center = nearestCenter(p);
    vec2 toCenter = p - center;
    vec2 uvOffset = normal * lens * Refraction;
    if (Magnify != 1.0) {
        uvOffset -= toCenter * (1.0 - 1.0 / max(Magnify, 0.1)) * (1.0 - lens);
    }
    vec2 baseUV = (p + uvOffset) / OutSize;

    // Хроматическая аберрация (радужная кромка)
    vec3 col;
    if (Dispersion > 0.0) {
        vec2 disp = normal * lens * Dispersion / OutSize;
        col.r = backdrop(baseUV + disp).r;
        col.g = backdrop(baseUV).g;
        col.b = backdrop(baseUV - disp).b;
    } else {
        col = backdrop(baseUV);
    }

    // Внутренняя тень (ощущение толщины)
    if (Depth > 0.0) {
        float shadow = clamp(distToEdge / max(Depth * 5.0, 1.0), 0.0, 1.0);
        col *= mix(0.5, 1.0, shadow);
    }

    // Тонкая внешняя кромка (Rim Light)
    float rimMask = clamp(1.0 - distToEdge / max(RimWidth, 0.5), 0.0, 1.0);
    col += vec3(rimMask * Rim);

    // Мягкий Френель к краям
    if (Fresnel > 0.0) {
        col += SpecularColor * (lens * Fresnel);
    }

    // Безопасная нормализация вектора света (фикс бага с NaN)
    vec2 lDir = vec2(0.0, -1.0);
    if (length(LightDir) > 0.0001) {
        lDir = normalize(LightDir);
    }

    // iOS 26 Непрерывная световая полоса вдоль контура
    float angleNormal = atan(normal.y, normal.x);
    float angleLight = atan(lDir.y, lDir.x);
    float wave = sin(angleNormal - angleLight + Time * 1.5) * 0.5 + 0.5;
    
    // Блики (Specular)
    float ndl = dot(normal, -lDir);
    if (ndl > 0.0) {
        float spec = pow(ndl, max(Sharpness, 1.0)) * Specular;
        // Смешиваем статичный блик со скользящей полосой волны
        spec += wave * pow(lens, 3.0) * Specular * 0.4;
        col += SpecularColor * spec;
    }

    // Процедурное зерно
    if (Grain > 0.0) {
        float noise = minee_valueNoise(p * 2.0 + vec2(Time * 10.0));
        col += vec3((noise - 0.5) * Grain);
    }

    // Цветокоррекция подложки
    col = minee_applyColorMods(col, Saturation, Brightness, Contrast);

    // Применение тинта (окрашивания), если задано в GUI
    if (TintCount > 0) {
        col = minee_blendTint(col, p, TintType, TintCount, TintLab, TintPos, TintDir, TintCenter, TintRadius);
    }

    fragColor = vec4(minee_toLinear(col), mask * vertexColor.a);
}

