import yaml
import os, sys
import subprocess
import shlex
import argparse
import time

SCRIPTS_DIR = os.path.dirname(os.path.realpath(__file__))
UNITS_INFERENCE_DIR = os.path.join(SCRIPTS_DIR, "..")
tool_excutable = os.path.join(SCRIPTS_DIR, "run-dljc-typecheck.sh")

def main(argv):
    parser = argparse.ArgumentParser()
    parser.add_argument('--corpus-file', dest='corpus_file', required=True)
    parser.add_argument('--corpus', dest='corpus')
    args = parser.parse_args()

    corpus_name = args.corpus if not args.corpus == None else os.path.splitext(args.corpus_file)[0]

    BENCHMARK_DIR = os.path.join(UNITS_INFERENCE_DIR, "benchmarks", corpus_name)

    print "----- Fetching corpus... -----"
    if not os.path.exists(BENCHMARK_DIR):
        print "Creating corpus dir {}.".format(BENCHMARK_DIR)
        os.makedirs(BENCHMARK_DIR)
        print "Corpus dir {} created.".format(BENCHMARK_DIR)

    print "Enter corpus dir {}.".format(BENCHMARK_DIR)
    os.chdir(BENCHMARK_DIR)

    projects = None
    with open (os.path.join(UNITS_INFERENCE_DIR, "benchmarks", args.corpus_file)) as projects_file:
        projects = yaml.load(projects_file)["paper"]

    for project_name, project_attrs in projects.iteritems():
        project_dir = os.path.join(BENCHMARK_DIR, project_name)
        if not os.path.exists(project_dir):
            # clone the branch if it is defined
            if "branch" in project_attrs and project_attrs["branch"]:
                git("clone", project_attrs["giturl"], "--depth", "1", "--branch", project_attrs["branch"])
            else:
                git("clone", project_attrs["giturl"], "--depth", "1")

    print "----- Fetching corpus done. -----"

    print "----- Running Units Typecheck on corpus... -----"

    successful_projects = list()
    failed_projects = list()

    for project_name, project_attrs in projects.iteritems():
        project_dir = os.path.join(BENCHMARK_DIR, project_name)
        os.chdir(project_dir)
        print "Enter directory: {}".format(project_dir)
        if project_attrs["clean"] == '' or project_attrs["build"] == '':
            print "Skip project {}, as there were no build/clean cmd.".format(project_name)
        print "Cleaning project..."
        subprocess.call(shlex.split(project_attrs["clean"]))
        print "Cleaning done."
        print "Running command: {}".format(tool_excutable + " " + project_attrs["build"])
        start = time.time()
        rtn_code = subprocess.call([tool_excutable, project_attrs["build"]])
        end = time.time()
        print "Return code is {}.".format(rtn_code)
        print "Time taken by {}: \t{}\t seconds".format(project_name, end - start)
        if not rtn_code == 0:
            failed_projects.append(project_name)
        else:
            successful_projects.append(project_name)

    if len(failed_projects) > 0:
        print "----- Typecheck failed on {} out of {} projects. -----".format(len(failed_projects), len(projects))
        print "  Successful projects are: {}.".format(successful_projects)
        print "  Failed projects are: {}.".format(failed_projects)
    else:
        print "----- Typecheck successfully inferred all {} projects. -----".format(len(projects))

    print "----- Running Units Typecheck on corpus done. -----"

    rtn_code = 1 if len(failed_projects) > 0 else 0

    sys.exit(rtn_code)

def git(*args):
    git_command = ['git'] + list(args)
    print("Running " + " ".join(git_command))
    return subprocess.check_call(git_command)

if __name__ == "__main__":
    main(sys.argv)
