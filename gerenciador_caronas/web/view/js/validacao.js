function limparCpf(cpf) { return (cpf || "").replace(/\D/g, ""); }

function cpfValido(cpf) {
  const numeros = limparCpf(cpf);
  if (numeros.length !== 11) return false;
  if (/^(\d)\1{10}$/.test(numeros)) return false;

  const digitos = numeros.split("").map(Number);

  let soma1 = 0;
  for (let i = 0; i < 9; i++) soma1 += digitos[i] * (10 - i);
  let d1 = (soma1 * 10) % 11;
  if (d1 === 10) d1 = 0;
  if (d1 !== digitos[9]) return false;

  let soma2 = 0;
  for (let i = 0; i < 10; i++) soma2 += digitos[i] * (11 - i);
  let d2 = (soma2 * 10) % 11;
  if (d2 === 10) d2 = 0;
  return d2 === digitos[10];
}

function formatarCpfEnquantoDigita(valor) {
  const n = limparCpf(valor).slice(0, 11);
  if (n.length <= 3) return n;
  if (n.length <= 6) return `${n.slice(0, 3)}.${n.slice(3)}`;
  if (n.length <= 9) return `${n.slice(0, 3)}.${n.slice(3, 6)}.${n.slice(6)}`;
  return `${n.slice(0, 3)}.${n.slice(3, 6)}.${n.slice(6, 9)}-${n.slice(9)}`;
}

function emailValido(email) { return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email || ""); }

function telefoneValido(telefone) {
  const numeros = (telefone || "").replace(/\D/g, "");
  return numeros.length === 10 || numeros.length === 11;
}

function senhaValida(senha) { return (senha || "").length >= 6; }

function marcarErro(idCampo, mensagem) {
  const input = document.getElementById(idCampo);
  const erroEl = document.getElementById(`erro-${idCampo}`);
  if (input) input.classList.add("invalido");
  if (erroEl) erroEl.textContent = mensagem;
}

function limparErro(idCampo) {
  const input = document.getElementById(idCampo);
  const erroEl = document.getElementById(`erro-${idCampo}`);
  if (input) input.classList.remove("invalido");
  if (erroEl) erroEl.textContent = "";
}

function mostrarMensagemGeral(elementId, mensagem, tipo) {
  const el = document.getElementById(elementId);
  if (!el) return;
  el.textContent = mensagem;
  el.className = `mensagem-geral ${tipo}`;
  el.hidden = mensagem === "";
}

function usuarioLogado() {
  const id = sessionStorage.getItem("usuarioId");
  const nome = sessionStorage.getItem("usuarioNome");
  return id ? { id, nome } : null;
}

function idUsuarioLogado() {
  const usuario = usuarioLogado();
  return usuario ? usuario.id : null;
}

function exigirLogin() {
  if (!usuarioLogado()) {
    window.location.href = "login.html";
    return false;
  }
  return true;
}

function renderizarAreaUsuario() {
  const area = document.getElementById("area-usuario");
  if (!area) return;
  const usuario = usuarioLogado();

  if (usuario) {
    area.innerHTML = `<span>Olá, ${usuario.nome}</span><a href="#" id="link-sair" class="link-sair">Sair</a>`;
    document.getElementById("link-sair").addEventListener("click", async (evento) => {
      evento.preventDefault();
      await encerrarSessaoNoServidor();
      sessionStorage.clear();
      window.location.href = "login.html";
    });
  } else {
    area.innerHTML = `<a href="login.html" class="link-sair">Entrar</a>`;
  }
}

async function encerrarSessaoNoServidor() {
  try {
    await fetch("../UsuarioServlet", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({ acao: "Logout" })
    });
  } catch (erro) {
    // mesmo se a chamada falhar, seguimos limpando o lado do cliente e redirecionando
  }
}

async function fetchComSessao(url, opcoes) {
  return await fetch(url, opcoes);
}