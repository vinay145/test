import React, { useState } from 'react';
import './App.css';

function ScoreCircle({ score }) {
  const radius = 54;
  const circumference = 2 * Math.PI * radius;
  const offset = circumference - (score / 100) * circumference;
  const color = score >= 75 ? '#27ae60' : score >= 50 ? '#f39c12' : '#e74c3c';

  return (
    <div className="score-circle-wrapper">
      <svg width="130" height="130" viewBox="0 0 130 130">
        <circle cx="65" cy="65" r={radius} fill="none" stroke="#e0e0e0" strokeWidth="10" />
        <circle
          cx="65" cy="65" r={radius} fill="none"
          stroke={color} strokeWidth="10"
          strokeDasharray={circumference}
          strokeDashoffset={offset}
          strokeLinecap="round"
          transform="rotate(-90 65 65)"
          style={{ transition: 'stroke-dashoffset 1s ease' }}
        />
      </svg>
      <div className="score-text">
        <span className="score-number" style={{ color }}>{score}</span>
        <span className="score-label">/ 100</span>
      </div>
    </div>
  );
}

function CategoryBar({ name, score }) {
  const color = score >= 75 ? '#27ae60' : score >= 50 ? '#f39c12' : '#e74c3c';
  return (
    <div className="category-bar">
      <div className="category-name">{name}</div>
      <div className="bar-container">
        <div
          className="bar-fill"
          style={{ width: `${score}%`, backgroundColor: color, transition: 'width 1s ease' }}
        />
      </div>
      <div className="category-score" style={{ color }}>{score}</div>
    </div>
  );
}

function App() {
  const [resumeText, setResumeText] = useState('');
  const [jobDescription, setJobDescription] = useState('');
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [activeTab, setActiveTab] = useState('strengths');

  const handleAnalyze = async () => {
    if (!resumeText.trim()) {
      setError('Please paste your resume text before analyzing.');
      return;
    }
    setError('');
    setLoading(true);
    setResult(null);

    try {
      const response = await fetch('http://localhost:8080/api/resume/analyze', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ resumeText, jobDescription }),
      });

      if (!response.ok) {
        throw new Error('Failed to analyze resume. Please try again.');
      }

      const data = await response.json();
      setResult(data);
    } catch (err) {
      setError(err.message || 'Could not connect to the server. Make sure the backend is running on port 8080.');
    } finally {
      setLoading(false);
    }
  };

  const handleClear = () => {
    setResumeText('');
    setJobDescription('');
    setResult(null);
    setError('');
  };

  return (
    <div className="app-container">
      <header className="app-header">
        <div className="header-content">
          <div className="header-icon"><span role="img" aria-label="document">📄</span></div>
          <div>
            <h1>Resume Score Checker</h1>
            <p>Analyze your resume and get an ATS compatibility score with actionable feedback</p>
          </div>
        </div>
      </header>

      <main className="main-content">
        <div className="input-section">
          <div className="input-card">
            <label className="input-label">
              <span className="label-icon" role="img" aria-label="resume">📝</span> Resume Text <span className="required">*</span>
            </label>
            <textarea
              className="textarea"
              placeholder="Paste your resume text here..."
              value={resumeText}
              onChange={(e) => setResumeText(e.target.value)}
              rows={14}
            />
          </div>

          <div className="input-card">
            <label className="input-label">
              <span className="label-icon" role="img" aria-label="job">💼</span> Job Description <span className="optional">(optional)</span>
            </label>
            <textarea
              className="textarea"
              placeholder="Paste the job description here for keyword matching analysis..."
              value={jobDescription}
              onChange={(e) => setJobDescription(e.target.value)}
              rows={14}
            />
          </div>
        </div>

        {error && <div className="error-banner"><span role="img" aria-label="warning">⚠️</span> {error}</div>}

        <div className="button-row">
          <button className="btn btn-primary" onClick={handleAnalyze} disabled={loading}>
            {loading ? <><span className="spinner" /> Analyzing...</> : <><span role="img" aria-label="search">🔍</span> Analyze Resume</>}
          </button>
          <button className="btn btn-secondary" onClick={handleClear}>
            <span role="img" aria-label="clear">🗑️</span> Clear
          </button>
        </div>

        {result && (
          <div className="results-section">
            <div className="results-header">
              <h2>Analysis Results</h2>
              <div className="grade-badge">Grade: {result.grade}</div>
            </div>

            <div className="results-top">
              <div className="score-card">
                <h3>Overall Score</h3>
                <ScoreCircle score={result.overallScore} />
                <p className="summary-text">{result.summary}</p>
              </div>

              <div className="categories-card">
                <h3>Category Breakdown</h3>
                {Object.entries(result.categoryScores).map(([name, score]) => (
                  <CategoryBar key={name} name={name} score={score} />
                ))}
              </div>
            </div>

            <div className="feedback-section">
              <div className="tabs">
                <button
                  className={`tab ${activeTab === 'strengths' ? 'active' : ''}`}
                  onClick={() => setActiveTab('strengths')}
                >
                  <span role="img" aria-label="check">✅</span> Strengths ({result.strengths.length})
                </button>
                <button
                  className={`tab ${activeTab === 'improvements' ? 'active' : ''}`}
                  onClick={() => setActiveTab('improvements')}
                >
                  <span role="img" aria-label="improvements">🔧</span> Improvements ({result.improvements.length})
                </button>
                {result.matchedKeywords && result.matchedKeywords.length > 0 && (
                  <button
                    className={`tab ${activeTab === 'keywords' ? 'active' : ''}`}
                    onClick={() => setActiveTab('keywords')}
                  >
                    <span role="img" aria-label="keywords">🔑</span> Keywords ({result.matchedKeywords.length})
                  </button>
                )}
              </div>

              <div className="tab-content">
                {activeTab === 'strengths' && (
                  <ul className="feedback-list strengths-list">
                    {result.strengths.length > 0
                      ? result.strengths.map((s, i) => <li key={i}><span role="img" aria-label="check">✅</span> {s}</li>)
                      : <li className="empty-state">No strengths identified yet. Improve your resume using the suggestions below.</li>
                    }
                  </ul>
                )}
                {activeTab === 'improvements' && (
                  <ul className="feedback-list improvements-list">
                    {result.improvements.length > 0
                      ? result.improvements.map((imp, i) => <li key={i}><span role="img" aria-label="tool">🔧</span> {imp}</li>)
                      : <li className="empty-state"><span role="img" aria-label="celebrate">🎉</span> Great job! No major improvements needed.</li>
                    }
                  </ul>
                )}
                {activeTab === 'keywords' && (
                  <div className="keywords-container">
                    {result.matchedKeywords.map((kw, i) => (
                      <span key={i} className="keyword-chip">{kw}</span>
                    ))}
                  </div>
                )}
              </div>
            </div>
          </div>
        )}
      </main>

      <footer className="app-footer">
        <p>Resume Score Checker — Built with Java Spring Boot &amp; React</p>
      </footer>
    </div>
  );
}

export default App;
