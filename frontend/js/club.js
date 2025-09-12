// 갤러리 렌더
const $carousel = document.getElementById("carousel");
const $dots = document.getElementById("dots");

const images = [
  "../images/justdoit.jpg",
  "../images/sori.jpg",
  "../images/tornado.jpg",
  "../images/trip.jpg",
];

const FALLBACK =
  "data:image/svg+xml;utf8,\
<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 300 300'>\
<rect width='300' height='300' fill='%23f3f4f6'/>\
<text x='50%' y='50%' dominant-baseline='middle' text-anchor='middle' fill='%239ca3af' font-size='14'>이미지 로딩 실패</text>\
</svg>";

images.forEach((src, i) => {
  // ✅ 슬라이드 래퍼
  const slide = document.createElement("div");
  slide.className = "slide";

  const img = document.createElement("img");
  img.src = src;
  img.alt = `동아리 이미지 ${i + 1}`;
  img.onerror = () => (img.src = FALLBACK); // ← 로딩 실패 대비

  slide.appendChild(img);
  $carousel.appendChild(slide);

  const dot = document.createElement("button");
  if (i === 0) dot.classList.add("is_active");
  dot.addEventListener("click", () => {
    $carousel.scrollTo({ left: i * $carousel.clientWidth, behavior: "smooth" });
  });
  $dots.appendChild(dot);
});

// 네비게이션/도트 업데이트
const $prev = document.getElementById("prevBtn");
const $next = document.getElementById("nextBtn");

function slideBy(dir) {
  const idx = Math.round($carousel.scrollLeft / $carousel.clientWidth);
  const nextIdx = Math.max(0, Math.min(images.length - 1, idx + dir));
  $carousel.scrollTo({
    left: nextIdx * $carousel.clientWidth,
    behavior: "smooth",
  });
}

$prev.addEventListener("click", () => slideBy(-1));
$next.addEventListener("click", () => slideBy(1));

const updateDots = () => {
  const idx = Math.round($carousel.scrollLeft / $carousel.clientWidth);
  [...$dots.children].forEach((b, i) =>
    b.classList.toggle("is_active", i === idx)
  );
};
$carousel.addEventListener("scroll", () => requestAnimationFrame(updateDots));
