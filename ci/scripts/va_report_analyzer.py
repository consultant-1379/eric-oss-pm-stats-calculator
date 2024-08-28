#!/usr/bin/env python3
#
# COPYRIGHT Ericsson 2023
#
# The copyright to the computer program(s) herein is the property of
# Ericsson Inc. The programs may be used and/or copied only with written
# permission from Ericsson Inc. or in accordance with the terms and
# conditions stipulated in the agreement/contract under which the
# program(s) have been supplied.
#

"""
usage: va_report_analyzer.py [-h] -c CONFIG -r REPORT [-e EXIT_CODE] [-v]

Vulnerability Analysis (VA) report analyzer script.

optional arguments:
  -h, --help            show this help message and exit
  -c CONFIG, --config CONFIG
                        Absolute path of the va-report.config file.
  -r REPORT, --report REPORT
                        Absolute path of the Vulnerability_Report_2.0.md file.
  -e EXIT_CODE, --exit-code EXIT_CODE
                        Exit code of the va_report script. If provided, and
                        the value is 0, 10 - return 0 as these exit codes
                        indicate normal behavior, 1, 2, 6 or 7 - return 0 as
                        these exit codes are suppressed errors, 5 - return 5
                        indicating that new Critical/High vulnerability found.
                        More info on exit codes
                        'https://gerrit-gamma.gic.ericsson.se/plugins/gitiles/adp-
                        cicd/bob-adp-release-
                        auto/+/master/vulnerability_analysis_2.0/#Switches-of-
                        the-va_report-script'
  -v, --verbose         Enable verbose output.

Extracts High and Critical severity vulnerability entries from the
vulnerability analysis report file, and compares the list with the mitigation
entries in va-report.config
"""

import re
import sys
import argparse


class Config:
  """Class modelling contents of a VA report config file."""

  def __init__(self, file: str) -> None:
    self._file = file
    self._mitigations = []
    self._parse_mitigations()

  @property
  def mitigations(self) -> list:
    return self._mitigations

  def _parse_mitigations(self) -> None:
    """Updates the contents of `_mitigations` array with mitigation
    entries found in the provided config file's 'mitigations:' section.
    Found mitigation entries are parsed and stored in their original
    vulnerability id form. More info 'https://gerrit-gamma.gic.ericsson.se/plugins/gitiles/adp-cicd/bob-adp-release-auto/+/master/vulnerability_analysis_2.0/README.md#Configuration-file-format_3'
    """

    with open(self._file, mode="r", encoding="utf8") as f:
      mitigations_attribute_name = "mitigations"
      ports_attribute_name = "open_ports"
      tenable_attribute_name = "tenable_sc_plugin_id"
      mitigations_section = False
      vulnerability_id_level = 2
      indent = 0  # indentation size used in file, typically 2 or 4 spaces
      for line in f.readlines():
        if not Config._is_comment(line) and len(line.strip()) > 0:
          curr_indent = Config._preceding_spaces(line)
          if indent == 0 and curr_indent > 0:
            assert curr_indent % 2 == 0, f"Indentation size should be a multiple of 2, got {curr_indent}"
            indent = curr_indent
          else:
            if not mitigations_section:
              if line.strip().lower() == f"{mitigations_attribute_name}:":
                mitigations_section = True
            else:
              level = curr_indent // indent
              if level == vulnerability_id_level:
                if line.strip().lower().startswith(ports_attribute_name) or \
                        line.strip().lower().startswith(tenable_attribute_name):
                  # not yet supported
                  pass
                else:
                  self._mitigations.append(Config._parse_mitigation(line))
              elif level < vulnerability_id_level:
                mitigations_section = False
                break

  @staticmethod
  def _is_comment(line: str) -> bool:
    return line.strip().startswith("#")

  @staticmethod
  def _as_cve_mitigation(line: str) -> str:
    """Try to parse a line as CVE mitigation entry.
    If there is a complete match, return the line in vulnerability id form,
    else return an empty string.
    """

    pattern = "^(cve)-(\d+)-?(\d+)?\s*:$"
    prog = re.compile(pattern, re.IGNORECASE)
    match = prog.match(line)
    if match:
      return "-".join(g for g in match.groups() if g)
    return ""

  @staticmethod
  def _as_xray_mitigation(line: str) -> str:
    """Try to parse a line as XRAY mitigation entry.
    If there is a complete match, return the line in vulnerability id form,
    else return an empty string.
    """

    pattern = "^(xray)-(\d+)-?(\d+)?\s*:$"
    prog = re.compile(pattern, re.IGNORECASE)
    match = prog.match(line)
    if match:
      return "-".join(g for g in match.groups() if g)
    return ""

  @staticmethod
  def _as_kubeaudit_mitigation(line: str) -> str:
    """Try to parse a line as Kubeaudit mitigation entry.
    If there is a complete match, return the line in vulnerability id form,
    else return an empty string.
    """

    pattern = "^([a-z]+)-([a-z]+)-((?:[a-z0-9]+-*)+)\s*:$"
    prog = re.compile(pattern, re.IGNORECASE)
    match = prog.match(line)
    if match:
      return f"{match.group(1)}: {match.group(2)}/{match.group(3)}"
    return ""

  @staticmethod
  def _as_kubesec_mitigation(line: str) -> str:
    """Try to parse a line as Kubesec mitigation entry.
    If there is a complete match, return the line in vulnerability id form,
    else return an empty string.
    """

    pattern = "^([a-z]+)/((?:[a-z0-9]+-*)+(?:\.[a-z]+)?)-([a-z]+)\s*:$"
    prog = re.compile(pattern, re.IGNORECASE)
    match = prog.match(line)
    if match:
      return f"{match.group(1)}/{match.group(2)}: {match.group(3)}"
    return ""

  @staticmethod
  def _as_zap_mitigation(line: str) -> str:
    """NOT FULLY SUPPORTED

    Try to parse a line as ZAP mitigation entry.
    If there is a complete match, return the line in vulnerability id form,
    else return an empty string.
    """

    pattern = "^(zap)-(.*)\s*:$"
    prog = re.compile(pattern, re.IGNORECASE)
    match = prog.match(line)
    if match:
      raise NotImplementedError("ZAP mitigations are not yet supported")
    return ""

  @staticmethod
  def _preceding_spaces(line: str) -> int:
    return len(line) - len(line.lstrip())

  @staticmethod
  def _parse_mitigation(line: str) -> str:
    line = line.strip()
    parsers = [Config._as_cve_mitigation,
               Config._as_xray_mitigation,
               Config._as_kubeaudit_mitigation,
               Config._as_kubesec_mitigation,
               Config._as_zap_mitigation]
    for parser in parsers:
      mitigation = parser(line)
      if mitigation:
        return mitigation
    raise ValueError(f"Couldn't parse mitigation '{line}'")


class Report:
  def __init__(self, file: str) -> None:
    self._file = file
    self._vulnerabilities = []
    self._parse_vulnerabilities()

  @property
  def vulnerabilities(self) -> list:
    return self._vulnerabilities

  def _parse_vulnerabilities(self) -> None:
    with open(self._file, mode="r", encoding="utf8") as f:
      summary_pattern = "^\|(c\d+|h\d+)\|(critical|high)\|(.+?)\|.*"
      prog = re.compile(summary_pattern, re.IGNORECASE)
      for line in f.readlines():
        match = prog.match(line)
        if match:
          self._vulnerabilities.append(match.group(3))


def get_diff(l1: list, l2: list) -> list:
  """Return list of items present in `l1` but not present in `l2`."""
  return [d for d in l1 if d not in l2]

def get_duplicates(l: list) -> list:
  """Return list of duplicate items in list."""
  return [d for d in l if l.count(d) > 1]


if __name__ == "__main__":
  parser = argparse.ArgumentParser(description = "Vulnerability Analysis (VA) report analyzer script.",
                                   epilog = "Extracts High and Critical severity vulnerability entries from \
                                             the vulnerability analysis report file, and compares the list \
                                             with the mitigation entries in va-report.config")
  parser.add_argument("-c", "--config",
                      required=True,
                      action="store",
                      help="Absolute path of the va-report.config file.")
  parser.add_argument("-r", "--report",
                      required=True,
                      action="store",
                      help="Absolute path of the Vulnerability_Report_2.0.md file.")
  parser.add_argument("-e", "--exit-code",
                      required=False,
                      action="store",
                      help="Exit code of the va_report script. If provided, and the value is \
                            0, 10 - return 0 as these exit codes indicate normal behavior, \
                            1, 2, 6 or 7 - return 0 as these exit codes are suppressed errors, \
                            5 - return 5 indicating that new Critical/High vulnerability found. \
                            More info on exit codes 'https://gerrit-gamma.gic.ericsson.se/plugins/gitiles/adp-cicd/bob-adp-release-auto/+/master/vulnerability_analysis_2.0/#Switches-of-the-va_report-script'")
  parser.add_argument("-v", "--verbose",
                      required=False,
                      action="store_true",
                      help="Enable verbose output.")

  args = parser.parse_args()
  exit_code = int(args.exit_code) if args.exit_code else 0
  run_analysis = True

  if exit_code in [0, 10]:  # normal behavior
    exit_code, run_analysis = 0, True
  elif exit_code in [4, 5]:  # vulnerability is found
    exit_code, run_analysis = exit_code, True
  else: # suppress bad va_report exit code
    exit_code, run_analysis = 0, False

  if run_analysis:
    try:
      config = Config(args.config)
      report = Report(args.report)

      if args.verbose:
        duplicate_mitigations = get_duplicates(config.mitigations)
        if len(duplicate_mitigations):
          print("------------ Duplicate mitigation entries in va-report.config ------------")
          print("\n".join(sorted(duplicate_mitigations)))

        extra_mitigations = get_diff(config.mitigations, report.vulnerabilities)
        if len(extra_mitigations):
          print("------------ Extra mitigation entries in va-report.config ------------")
          print("\n".join(sorted(extra_mitigations)))

      not_mitigated = get_diff(report.vulnerabilities, config.mitigations)
      if len(not_mitigated):
        print("------------ Not mitigated vulnerabilities ------------")
        print("\n".join(sorted(not_mitigated)))

    except (NotImplementedError, ValueError, AssertionError) as e:
      print(e)

  print(f"Exiting with [{exit_code}] exit code.")
  sys.exit(exit_code)