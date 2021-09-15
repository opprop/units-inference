import gen_microbenchmarks as gen
import subprocess 

# runs inference then returns the smt_solving_time in ms
def do_inference(file_name):
    # there may be an exception if inference fails, just ignore it
    try:
        stdout_lines = subprocess.check_output(['/home/opprop/units-inference/scripts/run-units-infer.sh', 'true', file_name]).decode().split('\n')
    except subprocess.CalledProcessError:
        pass
    # the inference writes statistics into this file
    with open('statistics.txt', 'r') as f:
        stats = f.read().split('\n')
        solve_line = [line for line in stats if line.startswith('smt_solving_time(millisec)')][0]
    return int(solve_line.split(' ', 1)[-1])

def print_results(solve_times):
    for op, files in solve_times.items():
        print()
        print(op)
        for file_name, solve_time in files.items():
            print(file_name + ', ', end='')
            print(solve_time)

# Experiment 1:
#   Try varying the number of groups and number of operations in each group.
#   Ensure the number of variables is equal to the number of operations and do not annotate the end result.
def gen_files_experiment_1(amt_groups, amt_per_group):
    file_names = {'add' : [], 'mult' : [] } #, 'comp' : []}
    for op in file_names:
        for amt_group in amt_groups:
            for amt in amt_per_group:
                if op == 'add':
                    file_names[op].append(gen.generate_file(add_groups = amt_group, add = amt))
                elif op == 'mult':
                    file_names[op].append(gen.generate_file(mult_groups = amt_group, mult = amt))
    return file_names

# Experiment 2:
#   Same as Experiment 1 but annotate the end result in each group of operations
def gen_files_experiment_2(amt_groups, amt_per_group):
    file_names = {'add' : [], 'mult' : [] } #, 'comp' : []}
    for op in file_names:
        for amt_group in amt_groups:
            for amt in amt_per_group:
                if op == 'add':
                    file_names[op].append(gen.generate_file(add_groups = amt_group, add = amt, add_end = True, add_annot = 75))
                elif op == 'mult':
                    file_names[op].append(gen.generate_file(mult_groups = amt_group, mult = amt, mult_end = True, mult_annot = 75))
    return file_names

# Experiment 3:
#   Fix the total number of operations and vary the number of groups
def gen_files_experiment_3(amt_groups, amt_ops):
    file_names = {'add' : [], 'mult' : [] } #, 'comp' : []}
    for op in file_names:
        for amt_group in amt_groups:
            amt = int(amt_ops / amt_group)
            if op == 'add':
                file_names[op].append(gen.generate_file(addgroups = amt_group, add = amt))
            elif op == 'mult':
                file_names[op].append(gen.generate_file(multgroups = amt_group, mult = amt))
    return file_names

def perform_experiment(experiment_id, num_replicates = 3):
   
    amt_groups = [1, 5, 10, 15, 20, 25, 30]
    amt_per_group = [1, 2, 3, 5, 7, 10, 15]
    
    # generate the microbenchmark files
    if experiment_id == 1:
        file_names = gen_files_experiment_1(amt_groups, amt_per_group)
    elif experiment_id == 2:
        file_names = gen_files_experiment_2(amt_groups, amt_per_group)
    elif experiment_id == 3:
        file_names = gen_files_experiment_3(amt_groups, 100)

    solve_times = dict()
    # setup
    for op, files in file_names.items():
        solve_times[op] = dict()
        for f in files:
            solve_times[op][f] = 0
    
    # do inference
    for i in range(num_replicates):
        for op, files in file_names.items():
            for f in files:
                solve_times[op][f] += do_inference(f)
    
    # take the average across all replicates
    for op, files in file_names.items():
        for f in files:
            # truncate to the nearest ms
            solve_times[op][f] = int(solve_times[op][f] / num_replicates)
   
    print_results(solve_times)

if __name__ == "__main__":
   
   perform_experiment(2, num_replicates = 1)
