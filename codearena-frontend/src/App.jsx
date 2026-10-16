import { useEffect, useState } from "react";
import Editor from "@monaco-editor/react";
import {
  Code2,
  LogOut,
  Search,
  Play,
  Send,
  CheckCircle2,
  XCircle,
  Clock3,
  MemoryStick,
  FileCode2,
  RefreshCw,
  ChevronRight,
  Terminal,
  Menu,
  X,
} from "lucide-react";
import api from "./services/api";
import "./App.css";

const starterCode = {
  java: `import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // Write your solution here

        sc.close();
    }
}`,
  python: `def main():
    # Write your solution here
    pass

if __name__ == "__main__":
    main()`,
  cpp: `#include <bits/stdc++.h>
using namespace std;

int main() {
    // Write your solution here
    return 0;
}`,
  javascript: `function main() {
    // Write your solution here
}

main();`,
};

function getToken(data) {
  return (
    data?.token ||
    data?.accessToken ||
    data?.jwt ||
    data?.access_token ||
    data?.data?.token ||
    ""
  );
}

function App() {
  const [token, setToken] = useState(() => localStorage.getItem("token") || "");
  const [authMode, setAuthMode] = useState("login");
  const [authForm, setAuthForm] = useState({
    username: "",
    email: "",
    password: "",
  });
  const [authError, setAuthError] = useState("");
  const [authLoading, setAuthLoading] = useState(false);

  const [problems, setProblems] = useState([]);
  const [selectedProblem, setSelectedProblem] = useState(null);
  const [search, setSearch] = useState("");
  const [loadingProblems, setLoadingProblems] = useState(false);
  const [problemError, setProblemError] = useState("");

  const [language, setLanguage] = useState("java");
  const [sourceCode, setSourceCode] = useState(starterCode.java);
  const [submitting, setSubmitting] = useState(false);
  const [submissions, setSubmissions] = useState([]);
  const [currentResult, setCurrentResult] = useState(null);
  const [resultError, setResultError] = useState("");
  const [activePanel, setActivePanel] = useState("results");
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  useEffect(() => {
    if (token) loadProblems();
  }, [token]);

  async function handleAuth(event) {
    event.preventDefault();
    setAuthError("");
    setAuthLoading(true);

    try {
      const endpoint = authMode === "login" ? "/auth/login" : "/auth/register";

      const payload =
        authMode === "login"
          ? { email: authForm.email, password: authForm.password }
          : {
              username: authForm.username,
              email: authForm.email,
              password: authForm.password,
            };

      const response = await api.post(endpoint, payload);
      let jwt = getToken(response.data);

      // Some registration endpoints create the account without logging in.
      if (authMode === "register" && !jwt) {
        setAuthMode("login");
        setAuthError("Registration successful. Please log in.");
        setAuthForm((old) => ({ ...old, password: "" }));
        return;
      }

      if (!jwt) {
        setAuthError(
          "Authentication succeeded, but no token was found. Check your login response format.",
        );
        return;
      }

      localStorage.setItem("token", jwt);
      setToken(jwt);
    } catch (error) {
      setAuthError(
        error.response?.data?.message ||
          error.response?.data?.error ||
          "Could not authenticate. Check your credentials and backend.",
      );
    } finally {
      setAuthLoading(false);
    }
  }

  function logout() {
    localStorage.removeItem("token");
    setToken("");
    setProblems([]);
    setSelectedProblem(null);
    setSubmissions([]);
    setCurrentResult(null);
  }

  async function loadProblems() {
    setLoadingProblems(true);
    setProblemError("");

    try {
      const response = await api.get("/problems");
      const data = Array.isArray(response.data)
        ? response.data
        : response.data?.content || response.data?.data || [];

      setProblems(data);

      setSelectedProblem((current) => {
        if (current) {
          return data.find((p) => p.id === current.id) || current;
        }
        return data[0] || null;
      });
    } catch (error) {
      setProblemError(
        error.response?.status === 401
          ? "Your session may have expired. Please log in again."
          : "Could not load problems. Check that your backend is running.",
      );
    } finally {
      setLoadingProblems(false);
    }
  }

  async function loadSubmissions() {
    try {
      const response = await api.get("/submissions/my");
      setSubmissions(Array.isArray(response.data) ? response.data : []);
    } catch {
      setSubmissions([]);
    }
  }

  function chooseLanguage(nextLanguage) {
    setLanguage(nextLanguage);
    setSourceCode(starterCode[nextLanguage]);
    setCurrentResult(null);
    setResultError("");
  }

  async function submitCode() {
    if (!selectedProblem) {
      setResultError("Select a problem first.");
      return;
    }

    if (!sourceCode.trim()) {
      setResultError("Enter your code before submitting.");
      return;
    }

    setSubmitting(true);
    setCurrentResult(null);
    setResultError("");
    setActivePanel("results");

    try {
      const response = await api.post("/submissions", {
        problemId: selectedProblem.id,
        language,
        sourceCode,
      });

      setCurrentResult(response.data);
      await loadSubmissions();
    } catch (error) {
      const message =
        error.response?.data?.message ||
        error.response?.data?.detail ||
        "Submission failed. Check your backend and test cases.";
      setResultError(message);
    } finally {
      setSubmitting(false);
    }
  }

  const filteredProblems = problems.filter((problem) =>
    String(problem.title || "")
      .toLowerCase()
      .includes(search.toLowerCase()),
  );

  if (!token) {
    return (
      <div className="auth-screen">
        <div className="auth-glow auth-glow-one" />
        <div className="auth-glow auth-glow-two" />

        <div className="auth-brand">
          <div className="brand-icon">
            <Code2 size={27} />
          </div>
          <span>
            Code<span className="brand-accent">Arena</span>
          </span>
        </div>

        <form className="auth-card" onSubmit={handleAuth}>
          <div className="auth-eyebrow">DEVELOPER PLATFORM</div>
          <h1>
            {authMode === "login" ? "Welcome back" : "Create your account"}
          </h1>
          <p className="muted">
            {authMode === "login"
              ? "Sign in and continue your coding journey."
              : "Join CodeArena and start solving problems."}
          </p>

          {authMode === "register" && (
            <label className="field">
              Username
              <input
                required
                value={authForm.username}
                onChange={(e) =>
                  setAuthForm({
                    ...authForm,
                    username: e.target.value,
                  })
                }
                placeholder="Choose a username"
                autoComplete="username"
              />
            </label>
          )}

          <label className="field">
            Email address
            <input
              type="email"
              required
              value={authForm.email}
              onChange={(e) =>
                setAuthForm({
                  ...authForm,
                  email: e.target.value,
                })
              }
              placeholder="you@example.com"
              autoComplete="email"
            />
          </label>

          <label className="field">
            Password
            <input
              type="password"
              required
              value={authForm.password}
              onChange={(e) =>
                setAuthForm({
                  ...authForm,
                  password: e.target.value,
                })
              }
              placeholder="Enter your password"
              autoComplete={
                authMode === "login" ? "current-password" : "new-password"
              }
            />
          </label>

          {authError && <div className="notice">{authError}</div>}

          <button className="primary-button auth-submit" disabled={authLoading}>
            {authLoading
              ? "Please wait..."
              : authMode === "login"
                ? "Sign in"
                : "Create account"}
            <ChevronRight size={18} />
          </button>

          <p className="auth-switch">
            {authMode === "login"
              ? "New to CodeArena?"
              : "Already have an account?"}{" "}
            <button
              type="button"
              className="text-button"
              onClick={() => {
                setAuthMode(authMode === "login" ? "register" : "login");
                setAuthError("");
              }}
            >
              {authMode === "login" ? "Create account" : "Sign in"}
            </button>
          </p>
        </form>

        <p className="auth-footer">PRACTICE. DEBUG. IMPROVE.</p>
      </div>
    );
  }

  return (
    <div className="app-shell">
      <header className="topbar">
        <button
          className="icon-button mobile-only"
          onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
          aria-label="Toggle problems menu"
        >
          {mobileMenuOpen ? <X size={20} /> : <Menu size={20} />}
        </button>

        <div className="top-brand">
          <div className="brand-icon small">
            <Code2 size={20} />
          </div>
          <span>
            Code<span className="brand-accent">Arena</span>
          </span>
          <span className="platform-tag">CODING PLATFORM</span>
        </div>

        <div className="topbar-right">
          <span className="connection-dot" /> Backend connected
          <button className="logout-button" onClick={logout}>
            <LogOut size={16} /> <span>Logout</span>
          </button>
        </div>
      </header>

      <main className="workspace">
        <aside
          className={`problem-sidebar ${mobileMenuOpen ? "mobile-open" : ""}`}
        >
          <div className="sidebar-heading">
            <div>
              <span className="section-kicker">WORKSPACE</span>
              <h2>Problems</h2>
            </div>
            <button
              className="icon-button"
              onClick={loadProblems}
              title="Refresh problems"
              aria-label="Refresh problems"
            >
              <RefreshCw size={16} />
            </button>
          </div>

          <div className="search-box">
            <Search size={16} />
            <input
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search problems..."
            />
          </div>

          <div className="problem-count">
            <span>ALL PROBLEMS</span>
            <span>{filteredProblems.length}</span>
          </div>

          {loadingProblems && (
            <p className="sidebar-message">Loading problems...</p>
          )}
          {problemError && <p className="sidebar-error">{problemError}</p>}

          <div className="problem-list">
            {filteredProblems.map((problem) => (
              <button
                key={problem.id}
                className={`problem-item ${
                  selectedProblem?.id === problem.id ? "selected" : ""
                }`}
                onClick={() => {
                  setSelectedProblem(problem);
                  setMobileMenuOpen(false);
                  setCurrentResult(null);
                  setResultError("");
                }}
              >
                <div className="problem-item-icon">
                  <FileCode2 size={17} />
                </div>
                <div className="problem-item-content">
                  <span className="problem-item-title">{problem.title}</span>
                  <span className="problem-item-meta">
                    <span
                      className={`difficulty ${String(
                        problem.difficulty || "EASY",
                      ).toLowerCase()}`}
                    >
                      {problem.difficulty || "Unrated"}
                    </span>
                    <span>#{problem.id}</span>
                  </span>
                </div>
                <ChevronRight className="problem-chevron" size={16} />
              </button>
            ))}

            {!loadingProblems &&
              !problemError &&
              filteredProblems.length === 0 && (
                <div className="empty-state">
                  <FileCode2 size={25} />
                  <p>No problems found.</p>
                </div>
              )}
          </div>

          <div className="sidebar-bottom">
            <div className="sidebar-bottom-icon">
              <Terminal size={17} />
            </div>
            <div>
              <strong>Keep practicing</strong>
              <p>Every problem makes you better.</p>
            </div>
          </div>
        </aside>

        <section className="main-workspace">
          <div className="breadcrumb">
            <span>Workspace</span>
            <ChevronRight size={14} />
            <span>Problems</span>
            {selectedProblem && (
              <>
                <ChevronRight size={14} />
                <strong>{selectedProblem.title}</strong>
              </>
            )}
          </div>

          {selectedProblem ? (
            <>
              <section className="problem-description">
                <div className="problem-title-row">
                  <div>
                    <span className="section-kicker">
                      PROBLEM #{selectedProblem.id}
                    </span>
                    <h1>{selectedProblem.title}</h1>
                  </div>
                  <span
                    className={`difficulty-pill ${String(
                      selectedProblem.difficulty || "EASY",
                    ).toLowerCase()}`}
                  >
                    {selectedProblem.difficulty || "Unrated"}
                  </span>
                </div>

                <div className="description-content">
                  {selectedProblem.description ||
                    "No problem description is available."}
                </div>

                {selectedProblem.constraintsText && (
                  <div className="detail-block">
                    <h3>Constraints</h3>
                    <pre>{selectedProblem.constraintsText}</pre>
                  </div>
                )}

                {(selectedProblem.sampleInput ||
                  selectedProblem.sampleOutput) && (
                  <div className="sample-grid">
                    {selectedProblem.sampleInput && (
                      <div className="sample-card">
                        <h3>Sample input</h3>
                        <pre>{selectedProblem.sampleInput}</pre>
                      </div>
                    )}
                    {selectedProblem.sampleOutput && (
                      <div className="sample-card">
                        <h3>Sample output</h3>
                        <pre>{selectedProblem.sampleOutput}</pre>
                      </div>
                    )}
                  </div>
                )}
              </section>

              <section className="editor-card">
                <div className="editor-toolbar">
                  <div className="editor-tab">
                    <FileCode2 size={16} />
                    <span>solution</span>
                    <span className="editor-dot" />
                  </div>

                  <label className="language-select-label">
                    <span>Language</span>
                    <select
                      value={language}
                      onChange={(e) => chooseLanguage(e.target.value)}
                    >
                      <option value="java">Java</option>
                      <option value="python">Python</option>
                      <option value="cpp">C++</option>
                      <option value="javascript">JavaScript</option>
                    </select>
                  </label>
                </div>

                <div className="monaco-wrap">
                  <Editor
                    height="340px"
                    language={
                      language === "python"
                        ? "python"
                        : language === "cpp"
                          ? "cpp"
                          : language === "javascript"
                            ? "javascript"
                            : "java"
                    }
                    theme="vs-dark"
                    value={sourceCode}
                    onChange={(value) => setSourceCode(value || "")}
                    options={{
                      minimap: { enabled: false },
                      fontSize: 14,
                      fontFamily: "'JetBrains Mono', Consolas, monospace",
                      lineNumbers: "on",
                      scrollBeyondLastLine: false,
                      automaticLayout: true,
                      tabSize: 4,
                      padding: { top: 14 },
                      wordWrap: "on",
                    }}
                  />
                </div>

                <div className="editor-footer">
                  <span className="editor-hint">
                    <span className="hint-dot" /> Autosave is not enabled
                  </span>
                  <div className="editor-actions">
                    <button
                      className="secondary-button"
                      onClick={() => {
                        setSourceCode(starterCode[language]);
                        setCurrentResult(null);
                        setResultError("");
                      }}
                    >
                      Reset code
                    </button>
                    <button
                      className="primary-button submit-button"
                      onClick={submitCode}
                      disabled={submitting}
                    >
                      {submitting ? (
                        <RefreshCw size={16} className="spin" />
                      ) : (
                        <Send size={16} />
                      )}
                      {submitting ? "Judging..." : "Submit solution"}
                    </button>
                  </div>
                </div>
              </section>

              <section className="results-card">
                <div className="results-header">
                  <div className="results-tabs">
                    <button
                      className={activePanel === "results" ? "active" : ""}
                      onClick={() => setActivePanel("results")}
                    >
                      <Terminal size={16} /> Results
                    </button>
                    <button
                      className={activePanel === "history" ? "active" : ""}
                      onClick={() => {
                        setActivePanel("history");
                        loadSubmissions();
                      }}
                    >
                      <Clock3 size={16} /> Submission history
                    </button>
                  </div>
                  <span className="result-status-label">JUDGE OUTPUT</span>
                </div>

                {activePanel === "results" ? (
                  <div className="result-body">
                    {submitting ? (
                      <div className="result-empty">
                        <RefreshCw size={23} className="spin" />
                        <p>Judge0 is running your solution...</p>
                        <span>This may take a few seconds.</span>
                      </div>
                    ) : resultError ? (
                      <div className="result-error">
                        <XCircle size={21} />
                        <div>
                          <strong>Submission failed</strong>
                          <p>{resultError}</p>
                        </div>
                      </div>
                    ) : currentResult ? (
                      <>
                        <div className="result-status-row">
                          {currentResult.status === "ACCEPTED" ? (
                            <CheckCircle2 className="accepted-icon" size={25} />
                          ) : (
                            <XCircle className="failed-icon" size={25} />
                          )}
                          <div>
                            <h3
                              className={
                                currentResult.status === "ACCEPTED"
                                  ? "accepted-text"
                                  : "failed-text"
                              }
                            >
                              {currentResult.status?.replaceAll("_", " ")}
                            </h3>
                            <p>
                              Submission #{currentResult.id} ·{" "}
                              {currentResult.language}
                            </p>
                          </div>
                        </div>

                        <div className="metrics-grid">
                          <div className="metric-card">
                            <Clock3 size={18} />
                            <span>Execution time</span>
                            <strong>
                              {currentResult.executionTimeSeconds == null
                                ? "—"
                                : `${currentResult.executionTimeSeconds} s`}
                            </strong>
                          </div>
                          <div className="metric-card">
                            <MemoryStick size={18} />
                            <span>Memory usage</span>
                            <strong>
                              {currentResult.memoryUsageKb == null
                                ? "—"
                                : `${currentResult.memoryUsageKb} KB`}
                            </strong>
                          </div>
                          <div className="metric-card">
                            <CheckCircle2 size={18} />
                            <span>Passed test cases</span>
                            <strong>
                              {currentResult.passedTestCases ?? 0} /{" "}
                              {currentResult.totalTestCases ?? 0}
                            </strong>
                          </div>
                        </div>

                        {currentResult.errorMessage && (
                          <div className="error-output">
                            <h4>Error details</h4>
                            <pre>{currentResult.errorMessage}</pre>
                          </div>
                        )}
                      </>
                    ) : (
                      <div className="result-empty">
                        <Play size={23} />
                        <p>Your results will appear here.</p>
                        <span>
                          Choose a problem, write your solution, and submit it.
                        </span>
                      </div>
                    )}
                  </div>
                ) : (
                  <div className="history-list">
                    {submissions.length === 0 ? (
                      <div className="result-empty">
                        <Clock3 size={23} />
                        <p>No submissions yet.</p>
                      </div>
                    ) : (
                      submissions.map((submission) => (
                        <button
                          key={submission.id}
                          className="history-item"
                          onClick={() => {
                            setCurrentResult(submission);
                            setActivePanel("results");
                          }}
                        >
                          <span className="history-icon">
                            {submission.status === "ACCEPTED" ? (
                              <CheckCircle2 size={19} />
                            ) : (
                              <XCircle size={19} />
                            )}
                          </span>
                          <span className="history-details">
                            <strong>
                              {submission.problemTitle ||
                                `Problem #${submission.problemId}`}
                            </strong>
                            <small>
                              #{submission.id} · {submission.language} ·{" "}
                              {submission.submittedAt || ""}
                            </small>
                          </span>
                          <span
                            className={`history-status ${
                              submission.status === "ACCEPTED"
                                ? "accepted-text"
                                : "failed-text"
                            }`}
                          >
                            {submission.status?.replaceAll("_", " ")}
                          </span>
                        </button>
                      ))
                    )}
                  </div>
                )}
              </section>
            </>
          ) : (
            <div className="no-problem">
              <div className="no-problem-icon">
                <Code2 size={32} />
              </div>
              <h1>Welcome to CodeArena</h1>
              <p>
                {problemError ||
                  "Select a problem from the sidebar to start coding."}
              </p>
              <button className="secondary-button" onClick={loadProblems}>
                <RefreshCw size={16} /> Load problems
              </button>
            </div>
          )}
        </section>
      </main>
    </div>
  );
}

export default App;
